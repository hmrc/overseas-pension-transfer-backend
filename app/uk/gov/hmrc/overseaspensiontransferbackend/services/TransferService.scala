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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.libs.json.*
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.TransferConnector
import uk.gov.hmrc.overseaspensiontransferbackend.models.*
import uk.gov.hmrc.overseaspensiontransferbackend.models.QtStatus.*
import uk.gov.hmrc.overseaspensiontransferbackend.models.audit.JourneySubmittedType.SubmissionSucceeded
import uk.gov.hmrc.overseaspensiontransferbackend.models.audit.{AuditUserInfo, JourneySubmittedType, ReportSubmittedAuditModel}
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication.{PsaUser, PspUser}
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream.*
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.*
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.UserAnswersTransformer
import uk.gov.hmrc.overseaspensiontransferbackend.validators.PathParameterValidator.isQtNumber
import uk.gov.hmrc.overseaspensiontransferbackend.validators.{Submission, SubmissionSchemaValidator, SubmissionValidator}

import java.time.{LocalDate, ZoneOffset}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransferService @Inject() (
    repository: SaveForLaterRepository,
    validator: SubmissionValidator,
    schemaValidator: SubmissionSchemaValidator,
    transformer: UserAnswersTransformer,
    connector: TransferConnector,
    auditService: AuditService
  )(implicit ec: ExecutionContext
  ) extends Logging {

  def submitTransfer(
      submission: NormalisedSubmission
    )(implicit hc: HeaderCarrier
    ): Future[Either[SubmissionError, SubmissionResponse]] =
    repository.get(submission.referenceId.value).flatMap {
      case Some(saved) =>
        validator.validate(Submission(saved, submission)) match {
          case Left(err)        =>
            Future.successful(Left(SubmissionTransformationError(err.message)))
          case Right(validated) =>
            val schemaErrors = schemaValidator.validate(Json.toJson(validated))
            if (schemaErrors.nonEmpty) {
              logger.warn(
                s"[submitTransfer] referenceId=${submission.referenceId} schema validation failed: ${schemaErrors.map(_.getMessage).mkString("; ")}"
              )
              Future.successful(Left(SubmissionTransformationError("Schema validation failed")))
            } else {
              val correlationId = UUID.randomUUID().toString
              connector
                .submitTransfer(validated, correlationId)
                .map {
                  case Right(success) =>
                    repository.clear(referenceId = submission.referenceId.value)
                    if (validated.transferringMember.isDefined) {
                      auditService.audit(
                        ReportSubmittedAuditModel.build(
                          referenceId              = saved.transferId,
                          journeyType              = SubmissionSucceeded,
                          correlationId            = correlationId,
                          failureReason            = None,
                          maybeQTNumber            = Some(success.qtNumber),
                          maybeMemberDetails       = validated.transferringMember.get.memberDetails,
                          maybeTransferDetails     = validated.transferDetails,
                          maybeAboutReceivingQROPS = validated.aboutReceivingQROPS,
                          maybeUserInfo            = Some(getAuditUserInfo(submission))
                        )
                      )
                    }

                    Right(SubmissionResponse(success.qtNumber, success.processingDate))
                  case Left(err)      =>
                    logger.info(s"[submitTransfer] referenceId=${submission.referenceId} ${err.log}")
                    auditService.audit(
                      ReportSubmittedAuditModel.build(
                        referenceId              = saved.transferId,
                        journeyType              = JourneySubmittedType.SubmissionFailed,
                        correlationId            = correlationId,
                        failureReason            = Some(err.log),
                        maybeQTNumber            = None,
                        maybeMemberDetails       = None,
                        maybeTransferDetails     = None,
                        maybeAboutReceivingQROPS = None,
                        maybeUserInfo            = None
                      )
                    )
                    Left(mapDownstream(err))
                }
                .recover { case _ => Left(SubmissionFailed) }
            }

        }
      case None        =>
        logger.info(
          s"[submitTransfer] referenceId=${submission.referenceId} No submission found in save for later repository"
        )
        Future.successful(
          Left(
            SubmissionTransformationError(
              s"No prepared submission for referenceId ${submission.referenceId}"
            )
          )
        )
    }

  private def getAuditUserInfo(submission: NormalisedSubmission): AuditUserInfo = {
    val (affinityGroup, userId, userType) = submission.authenticatedUser match {
      case u @ PsaUser(psaId, _, _, affinityGroup) => (affinityGroup, psaId, u.userType)
      case u @ PspUser(pspId, _, _, affinityGroup) => (affinityGroup, pspId, u.userType)
    }
    AuditUserInfo(
      userType,
      affinityGroup,
      userId,
      submission.maybeAssociatedPsaId
    )
  }

  private def mapDownstream(error: DownstreamError): SubmissionError = error match {
    case EtmpValidationError(_, _, _) | HipBadRequest(_, _, _, _) | HipOriginFailures(_, _) | UnsupportedMedia =>
      SubmissionTransformationError("Submission failed validation")

    case _ =>
      SubmissionFailed
  }

  def getTransfer(
      referenceId: String,
      pstr: String,
      qtStatus: QtStatus,
      versionNumber: Option[String]
    )(implicit hc: HeaderCarrier
    ): Future[Either[UpstreamErrorResponse, UserAnswersDTO]] = {
    (qtStatus, versionNumber) match {
      case (InProgress, None)                                               =>
        repository.get(referenceId).map {
          case Some(transferDetails) => deconstructSavedAnswers(transferDetails)
          case None                  =>
            logger.warn(s"In Progress record not found in SaveForLaterRepository for $referenceId")
            Left(UpstreamErrorResponse("NOT_FOUND", NOT_FOUND))
        }
      case (AmendInProgress, Some(version)) if isQtNumber(referenceId)      =>
        for {
          transferFromRepo      <- repository.get(referenceId)
          transferNotFoundInRepo = transferFromRepo.isEmpty
          transfer              <- if (transferNotFoundInRepo) {
                                     fetchTransferFromDownstream(pstr, referenceId, version)
                                   } else {
                                     Future(deconstructSavedAnswers(transferFromRepo.get))
                                   }
        } yield transfer
      case (Compiled | Submitted, Some(version)) if isQtNumber(referenceId) =>
        fetchTransferFromDownstream(pstr, referenceId, version)
      case _                                                                =>
        logger.error(s"Request parameters are invalid - transfer status: $qtStatus and  version: $versionNumber")
        Future(Left[UpstreamErrorResponse, UserAnswersDTO](UpstreamErrorResponse("Request parameters are invalid", INTERNAL_SERVER_ERROR)))
    }
  }

  private def fetchTransferFromDownstream(
      pstr: String,
      transferId: String,
      versionNumber: String
    )(implicit hc: HeaderCarrier
    ): Future[Either[UpstreamErrorResponse, UserAnswersDTO]] =
    connector.getTransfer(pstr, transferId, versionNumber) map {
      case Right(downstreamTransferDetails) =>
        downstreamTransferDetails.json.validate[DownstreamTransferData] match {
          case JsSuccess(validResponse, _) =>
            deconstructSavedAnswers(validResponse.toSavedUserAnswers)
          case er: JsError                 =>
            logger.error(s"(${transferId} - ${versionNumber}) Error validating transfer details from downstream: ${er.errors}")
            Left(UpstreamErrorResponse(s"Json Validation error in the transfer recieved from downstream", INTERNAL_SERVER_ERROR))
        }
      case Left(x)                          => Left(x)
    }

//ToDo: Can be made common for this and Save for later service
  private def deconstructSavedAnswers(
      savedUserAnswers: SavedUserAnswers
    ): Either[UpstreamErrorResponse, UserAnswersDTO] =
    transformer.deconstruct(Json.toJsObject(savedUserAnswers.data)) match {
      case Right(jsObject) =>
        Right(
          UserAnswersDTO(savedUserAnswers.transferId, savedUserAnswers.pstr, jsObject, savedUserAnswers.lastUpdated)
        )
      case Left(jsError)   =>
        logger.error(
          s"(${savedUserAnswers.transferId}) Error deconstructing from SaveForLaterRepository: ${jsError.errors}"
        )
        Left(UpstreamErrorResponse(s"Unable to deconstruct json with error: $jsError", INTERNAL_SERVER_ERROR))
    }

  def getAllTransfers(
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
        logger.warn("[getAllTransfers] in-progress lookup failed with error:", e)
        Seq.empty
      }

    for {
      dsEither   <- downstreamEitherF
      inProgress <- inProgressF
    } yield {
      def amendInProgressRecords =
        inProgress.filter(_.qtStatus.contains(AmendInProgress)).map(_.transferId)

      dsEither match {
        case Right(ds) =>
          val submitted = DownstreamAllTransfersData.toAllTransferItems(pstrNumber, ds)
          Right(
            AllTransfersResponse(inProgress.filterNot(_.qtStatus.contains(AmendInProgress)) ++ submitted.map { record =>
              if (amendInProgressRecords.contains(record.transferId)) {
                record.copy(qtStatus = Some(AmendInProgress))
              } else {
                record
              }
            })
          )
        case Left(e)   =>
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
