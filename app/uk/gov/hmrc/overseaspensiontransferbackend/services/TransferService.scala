/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.overseaspensiontransferbackend.services

import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.TransferConnector
import uk.gov.hmrc.overseaspensiontransferbackend.models._
import uk.gov.hmrc.overseaspensiontransferbackend.models.audit.JourneySubmittedType.SubmissionSucceeded
import uk.gov.hmrc.overseaspensiontransferbackend.models.audit.{JourneySubmittedType, ReportSubmittedAuditModel}
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream._
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.{GetEtmpRecord, GetSaveForLaterRecord, GetSpecificTransferHandler, UserAnswersDTO}
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer._
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.UserAnswersTransformer
import uk.gov.hmrc.overseaspensiontransferbackend.validators.SubmissionValidator

import java.time.{LocalDate, ZoneOffset}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait TransferService {
  def submitTransfer(submission: NormalisedSubmission)(implicit hc: HeaderCarrier): Future[Either[SubmissionError, SubmissionResponse]]
  def getAllTransfers(pstrNumber: PstrNumber)(implicit hc: HeaderCarrier, config: AppConfig): Future[Either[AllTransfersResponseError, AllTransfersResponse]]

  def getTransfer(
      transferType: Either[TransferRetrievalError, GetSpecificTransferHandler]
    )(implicit hc: HeaderCarrier
    ): Future[Either[TransferRetrievalError, UserAnswersDTO]]
}

@Singleton
class TransferServiceImpl @Inject() (
    repository: SaveForLaterRepository,
    validator: SubmissionValidator,
    transformer: UserAnswersTransformer,
    connector: TransferConnector,
    auditService: AuditService
  )(implicit ec: ExecutionContext
  ) extends TransferService with Logging {

  override def submitTransfer(submission: NormalisedSubmission)(implicit hc: HeaderCarrier): Future[Either[SubmissionError, SubmissionResponse]] =
    repository.get(submission.referenceId).flatMap {
      case Some(saved) =>
        validator.validate(saved) match {
          case Left(err)        =>
            Future.successful(Left(SubmissionTransformationError(err.message)))
          case Right(validated) =>
            connector.submitTransfer(validated).map {
              case Right(success) =>
                // TODO: the session store for the dashboards should be updated for the newly
                //  received QT Reference & QT status = submitted (when this repo is implemented)
                repository.clear(referenceId = submission.referenceId)

                if (validated.saved.data.transferringMember.isDefined) {
                  auditService.audit(
                    ReportSubmittedAuditModel.build(
                      validated.saved.referenceId,
                      SubmissionSucceeded,
                      None,
                      Some(success.qtNumber),
                      validated.saved.data.transferringMember.get.memberDetails,
                      validated.saved.data.transferDetails,
                      validated.saved.data.aboutReceivingQROPS
                    )
                  )
                }

                Right(SubmissionResponse(success.qtNumber))
              case Left(err)      =>
                logger.info(s"[submitTransfer] referenceId=${submission.referenceId} ${err.log}")
                auditService.audit(
                  ReportSubmittedAuditModel.build(
                    validated.saved.referenceId,
                    JourneySubmittedType.SubmissionFailed,
                    Some(err.log),
                    None,
                    None,
                    None,
                    None
                  )
                )
                Left(mapDownstream(err))
            }.recover { case _ => Left(SubmissionFailed) }
        }
      case None        =>
        logger.info(s"[submitTransfer] referenceId=${submission.referenceId} No submission found in save for later repository")
        Future.successful(Left(SubmissionTransformationError(
          s"No prepared submission for referenceId ${submission.referenceId}"
        )))
    }

  private def mapDownstream(e: DownstreamError): SubmissionError = e match {
    case EtmpValidationError(_, _, _) |
        HipBadRequest(_, _, _, _) |
        HipOriginFailures(_, _) |
        UnsupportedMedia =>
      SubmissionTransformationError("Submission failed validation")

    case Unauthorized |
        Forbidden |
        NotFound |
        ServerError |
        ServiceUnavailable |
        Unexpected(_, _) =>
      SubmissionFailed
  }

  def getTransfer(
      transferType: Either[TransferRetrievalError, GetSpecificTransferHandler]
    )(implicit hc: HeaderCarrier
    ): Future[Either[TransferRetrievalError, UserAnswersDTO]] = {
    transferType match {
      case Right(GetSaveForLaterRecord(transferId, _, InProgress))                   =>
        repository.get(transferId) map {
          case Some(userAnswers) =>
            deconstructSavedAnswers(userAnswers)
          case None              =>
            logger.error(s"[TransferService][getTransfer] Unable to find transferId: $transferId from save-for-later")
            Left(TransferNotFound(s"Unable to find transferId: $transferId from save-for-later"))
        }
      case Right(GetEtmpRecord(qtNumber, pstr, AmendInProgress, versionNumber))      =>
        repository.get(qtNumber.value) flatMap {
          case Some(userAnswers) =>
            Future.successful(deconstructSavedAnswers(userAnswers))
          case None              =>
            connector.getTransfer(pstr, qtNumber, versionNumber) flatMap {
              case Right(value) =>
                val savedUserAnswers = value.toSavedUserAnswers
                repository.set(savedUserAnswers) map {
                  case true  => deconstructSavedAnswers(savedUserAnswers)
                  case false =>
                    logger.error(s"[TransferService][getTransfer] Unable to set AmendInProgress for transferId: $qtNumber.value in save-for-later")
                    Left(TransferNotFound(s"Unable to set AmendInProgress for transferId: $qtNumber.value in save-for-later"))
                }
              case Left(err)    =>
                logger.error(s"[TransferService][getTransfer] Unable to find transferId: $qtNumber from HoD: ${err.log}")
                Future.successful(Left(TransferNotFound(s"Unable to find transferId: ${qtNumber.value} from HoD")))
            }
        }
      case Right(GetEtmpRecord(qtNumber, pstr, Submitted | Compiled, versionNumber)) =>
        connector.getTransfer(pstr, qtNumber, versionNumber) map {
          case Right(value) => deconstructSavedAnswers(value.toSavedUserAnswers)
          case Left(err)    =>
            logger.error(s"[TransferService][getTransfer] Unable to find transferId: $qtNumber from HoD: ${err.log}")
            Left(TransferNotFound(s"Unable to find transferId: ${qtNumber.value} from HoD"))
        }
    }
  }

  private def deconstructSavedAnswers(savedUserAnswers: SavedUserAnswers): Either[TransferRetrievalError, UserAnswersDTO] = {
    transformer.deconstruct(Json.toJsObject(savedUserAnswers.data)) match {
      case Right(jsObject) => Right(UserAnswersDTO(savedUserAnswers.referenceId, savedUserAnswers.pstr, jsObject, savedUserAnswers.lastUpdated))
      case Left(jsError)   =>
        logger.error(s"[TransferService][getTransfer] to deconstruct transferId: ${savedUserAnswers.referenceId} json with error: ${jsError.errors}")
        Left(TransferDeconstructionError(s"Unable to deconstruct json with error: $jsError"))
    }
  }

  override def getAllTransfers(
      pstrNumber: PstrNumber
    )(implicit hc: HeaderCarrier,
      config: AppConfig
    ): Future[Either[AllTransfersResponseError, AllTransfersResponse]] = {
    val toDate: LocalDate   = LocalDate.now(ZoneOffset.UTC)
    val fromDate: LocalDate = toDate.minusYears(config.getAllTransfersYearsOffset)

    val downstreamEitherF: Future[Either[DownstreamError, DownstreamAllTransfersData]] =
      connector.getAllTransfers(
        pstrNumber = pstrNumber,
        fromDate   = fromDate,
        toDate     = toDate,
        qtRef      = None
      )

    val inProgressF: Future[Seq[AllTransfersItem]] =
      repository.getRecords(pstrNumber).recover { case e =>
        logger.warn(s"[getAllTransfers] in-progress lookup failed for pstr=${pstrNumber.normalised}", e)
        Seq.empty
      }

    for {
      dsEither   <- downstreamEitherF
      inProgress <- inProgressF
    } yield {
      dsEither match {
        case Right(ds) =>
          val submitted = DownstreamAllTransfersData.toAllTransferItems(pstrNumber, ds)
          Right(AllTransfersResponse(inProgress ++ submitted))
        case Left(e)   =>
          logger.info(s"[getAllTransfers] pstr=${pstrNumber.normalised} ${e.log}")
          if (inProgress.nonEmpty) {
            Right(AllTransfersResponse(inProgress))
          } else {
            e match {
              case NoTransfersFound => Left(NoTransfersFoundResponse)
              case _                => Left(UnexpectedError(s"Unable to get all transfers for ${pstrNumber.value}"))
            }
          }
      }
    }
  }
}
