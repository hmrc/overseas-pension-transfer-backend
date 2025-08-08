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
import uk.gov.hmrc.overseaspensiontransferbackend.models._
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.{NormalisedSubmission, QtNumber}
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.validators.SubmissionValidator

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait SubmissionService {
  def submitAnswers(submission: NormalisedSubmission)(implicit hc: HeaderCarrier): Future[Either[SubmissionError, SubmissionResponse]]
}

@Singleton
class SubmissionServiceImpl @Inject() (
    repository: SaveForLaterRepository,
    validator: SubmissionValidator
  )(implicit ec: ExecutionContext
  ) extends SubmissionService {

  override def submitAnswers(submission: NormalisedSubmission)(implicit hc: HeaderCarrier): Future[Either[SubmissionError, SubmissionResponse]] =
    repository.get(submission.referenceId).flatMap {
      case Some(saved) =>
        validator.validate(saved) match {
          case Left(err)        =>
            Future.successful(Left(SubmissionTransformationError(err.message)))
          case Right(validated) => ???
        }
      case None        =>
        Future.successful(Left(SubmissionTransformationError(
          s"No prepared submission for referenceId ${submission.referenceId}"
        )))
    }
}

@Singleton
class DummySubmissionServiceImpl @Inject() (
    implicit ec: ExecutionContext
  ) extends SubmissionService {

  override def submitAnswers(submission: NormalisedSubmission)(implicit hc: HeaderCarrier): Future[Either[SubmissionError, SubmissionResponse]] = {
    Future.successful(Right(SubmissionResponse(QtNumber("QT123456"))))
  }
}
