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

import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.SubmissionDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.{NormalisedSubmission, QtNumber}

import scala.concurrent.Future

sealed trait SubmissionError

object SubmissionError {
  final case class TransformationError(msg: String) extends SubmissionError
  final case object SubmissionFailed                extends SubmissionError
}

trait SubmissionService {
  def submitAnswers(submission: NormalisedSubmission)(implicit hc: HeaderCarrier): Future[Either[SubmissionError, SubmissionResponse]]
}

class SubmissionServiceImpl extends SubmissionService {
  override def submitAnswers(submission: NormalisedSubmission)(implicit hc: HeaderCarrier): Future[Either[SubmissionError, SubmissionResponse]] = ???
}

case class SubmissionResponse(qtNumber: QtNumber)

object SubmissionResponse {
  implicit val format: OFormat[SubmissionResponse] = Json.format[SubmissionResponse]
}
