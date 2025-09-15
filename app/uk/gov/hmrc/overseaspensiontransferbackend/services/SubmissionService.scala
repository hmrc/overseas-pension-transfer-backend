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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.SubmissionConnector
import uk.gov.hmrc.overseaspensiontransferbackend.models.PstrNumber
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission._
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream._
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.validators.SubmissionValidator

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait SubmissionService {
  def submitAnswers(submission: NormalisedSubmission)(implicit hc: HeaderCarrier): Future[Either[SubmissionError, SubmissionResponse]]
  def getAllSubmissions(pstrNumber: PstrNumber)(implicit hc: HeaderCarrier): Future[Either[SubmissionGetAllError, SubmissionGetAllResponse]]
}

@Singleton
class SubmissionServiceImpl @Inject() (
    repository: SaveForLaterRepository,
    validator: SubmissionValidator,
    connector: SubmissionConnector
  )(implicit ec: ExecutionContext
  ) extends SubmissionService {

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
              case Left(e)        => Left(mapDownstream(e))
            }.recover { case _ => Left(SubmissionFailed) }
        }
      case None        =>
        Future.successful(Left(SubmissionTransformationError(
          s"No prepared submission for referenceId ${submission.referenceId}"
        )))
    }

  // TODO: Confirm what we want to send to the frontend
  private def mapDownstream(e: DownstreamSubmittedError): SubmissionError = e match {
    case EtmpValidationSubmittedError(_, _, _) |
        HipBadRequest(_, _, _, _) |
        HipOriginFailures(_, _) |
        UnsupportedMedia =>
      SubmissionTransformationError("Submission failed validation")

    case Unauthorized |
        Forbidden |
        NotFound |
        ServerSubmittedError$ |
        ServiceUnavailable |
        Unexpected(_, _) =>
      SubmissionFailed

  }

  override def getAllSubmissions(pstrNumber: PstrNumber)(implicit hc: HeaderCarrier): Future[Either[SubmissionGetAllError, SubmissionGetAllResponse]] = ???
}

@Singleton
class DummySubmissionServiceImpl @Inject() (
    implicit ec: ExecutionContext
  ) extends SubmissionService {

  override def submitAnswers(submission: NormalisedSubmission)(implicit hc: HeaderCarrier): Future[Either[SubmissionError, SubmissionResponse]] = {
    Future.successful(Right(SubmissionResponse(QtNumber("QT123456"))))
  }

  override def getAllSubmissions(pstrNumber: PstrNumber)(implicit hc: HeaderCarrier): Future[Either[SubmissionGetAllError, SubmissionGetAllResponse]] =
    Future.successful(Right(SubmissionGetAllResponse()))
}
