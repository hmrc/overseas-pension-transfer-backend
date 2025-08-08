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

package uk.gov.hmrc.overseaspensiontransferbackend.validators

import uk.gov.hmrc.overseaspensiontransferbackend.models.SavedUserAnswers

import javax.inject.{Inject, Singleton}

sealed trait ValidationResponse
case class ValidatedSubmission(saved: SavedUserAnswers) extends ValidationResponse
case class ValidationError(message: String)             extends ValidationResponse

trait SubmissionValidator {
  def validate(prepared: SavedUserAnswers): Either[ValidationError, ValidatedSubmission]
}

@Singleton
class DummySubmissionValidatorImpl @Inject() extends SubmissionValidator {

  override def validate(prepared: SavedUserAnswers): Either[ValidationError, ValidatedSubmission] =
    Right(ValidatedSubmission(prepared))
}
