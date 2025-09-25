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
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.SubmissionConnector
import uk.gov.hmrc.overseaspensiontransferbackend.models._
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream._
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.{GetEtmpRecord, GetSaveForLaterRecord, GetSpecificTransferHandler, UserAnswersDTO}
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission._
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.UserAnswersTransformer
import uk.gov.hmrc.overseaspensiontransferbackend.validators.SubmissionValidator

import java.time.{LocalDate, ZoneOffset}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait SubmissionService {
  def submitAnswers(submission: NormalisedSubmission)(implicit hc: HeaderCarrier): Future[Either[SubmissionError, SubmissionResponse]]
  def getAllTransfers(pstrNumber: PstrNumber)(implicit hc: HeaderCarrier, config: AppConfig): Future[Either[AllTransfersResponseError, AllTransfersResponse]]

  def getTransfer(
      transferType: Either[TransferRetrievalError, GetSpecificTransferHandler]
    )(implicit hc: HeaderCarrier
    ): Future[Either[TransferRetrievalError, UserAnswersDTO]]
}

@Singleton
class SubmissionServiceImpl @Inject() (
    repository: SaveForLaterRepository,
    validator: SubmissionValidator,
    transformer: UserAnswersTransformer,
    connector: SubmissionConnector
  )(implicit ec: ExecutionContext
  ) extends SubmissionService with Logging {

  override def submitAnswers(submission: NormalisedSubmission)(implicit hc: HeaderCarrier): Future[Either[SubmissionError, SubmissionResponse]] =
    repository.get(submission.referenceId).flatMap {
      case Some(saved) =>
        validator.validate(saved) match {
          case Left(err)        =>
            Future.successful(Left(SubmissionTransformationError(err.message)))
          case Right(validated) =>
            connector.submit(validated).map {
              case Right(success) =>
                // TODO: the session store for the dashboards should be updated for the newly
                //  received QT Reference & QT status = submitted (when this repo is implemented)
                repository.clear(referenceId = submission.referenceId)
                Right(SubmissionResponse(success.qtNumber))
              case Left(err)      => {
                logger.info(s"[submitAnswers] referenceId=${submission.referenceId} ${err.log}")
                Left(mapDownstream(err))
              }
            }.recover { case _ => Left(SubmissionFailed) }
        }
      case None        =>
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
            logger.error(s"[SubmissionService][getTransfer] Unable to find transferId: $transferId from save-for-later")
            Left(TransferNotFound(s"Unable to find transferId: $transferId from save-for-later"))
        }
      case Right(GetEtmpRecord(qtNumber, pstr, Submitted | Compiled, versionNumber)) =>
        connector.getTransfer(pstr, qtNumber, versionNumber) map {
          case Right(value) => deconstructSavedAnswers(value.toSavedUserAnswers)
          case Left(err)    =>
            logger.error(s"[SubmissionService][getTransfer] Unable to find transferId: $qtNumber from HoD: ${err.log}")
            Left(TransferNotFound(s"Unable to find transferId: ${qtNumber.value} from HoD"))
        }

      case Left(transferRetrievalError) => Future.successful(Left(transferRetrievalError))
    }
  }

  private def deconstructSavedAnswers(savedUserAnswers: SavedUserAnswers): Either[TransferRetrievalError, UserAnswersDTO] = {
    transformer.deconstruct(Json.toJsObject(savedUserAnswers.data)) match {
      case Right(jsObject) => Right(UserAnswersDTO(savedUserAnswers.referenceId, jsObject, savedUserAnswers.lastUpdated))
      case Left(jsError)   =>
        logger.error(s"[SubmissionService][getTransfer] to deconstruct transferId: ${savedUserAnswers.referenceId} json with error: ${jsError.errors}")
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

    /* This is a simplified version of the get all transfers service layer that just returns the last 10 years of transfers,
    It will need to be updated with in progress transfers (when we've indexed the mongo by scheme id) and perhaps later with
    from and to dates and utilising the qt reference functionality. */
    connector.getAllTransfers(pstrNumber = pstrNumber, fromDate = fromDate, toDate = toDate, qtRef = None).map {
      case Right(downstream) =>
        val items = downstream.success.qropsTransferOverview.map { r =>
          AllTransfersItem(
            transferReference = None,
            qtReference       = Some(QtNumber(r.qtReference)),
            qtVersion         = Some(r.qtVersion),
            nino              = Some(r.nino),
            memberFirstName   = Some(r.firstName),
            memberSurname     = Some(r.lastName),
            submissionDate    = Some(r.qtDate),
            // TODO: Add lastUpdated once the in progress transfers have been added, it should be the last time the in progress transfer was updated
            lastUpdated       = None,
            qtStatus          = Some(QtStatus(r.qtStatus)),
            pstrNumber        = Some(pstrNumber)
          )
        }
        Right(AllTransfersResponse(items))

      case Left(err) =>
        logger.info(s"[getAllTransfers] pstr=${pstrNumber.normalised} ${err.log}")

        err match {
          case NoTransfersFound => Left(NoTransfersFoundResponse)
          case _                => Left(UnexpectedError(s"Unable to get all transfers for ${pstrNumber.value}"))
        }
    }
  }
}
