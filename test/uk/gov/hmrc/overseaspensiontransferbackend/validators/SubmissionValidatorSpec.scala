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

import org.scalatest.freespec.AnyFreeSpec
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication.{Psp, PspId}
import uk.gov.hmrc.overseaspensiontransferbackend.models.{Declaration, QtDeclaration}

class SubmissionValidatorSpec extends AnyFreeSpec with SpecBase {

  "SubmissionValidatorImpl" - {

    "must return Right(ValidatedSubmission) when valid psa submission is passed" in {
      val validator = new SubmissionValidatorImpl()

      val result = validator.validate(samplePsaSubmission)

      result mustBe Right(samplePsaSubmission)
    }

    "must return Right(ValidatedSubmission) when valid psp submission is passed" in {
      val validator = new SubmissionValidatorImpl()

      val result = validator.validate(samplePspSubmission)

      result mustBe Right(samplePspSubmission)
    }

    "must return a Left of ValidationError when Psa submits with no Psa Declaration" in {
      val validator = new SubmissionValidatorImpl()

      val result = validator.validate(samplePsaSubmission.copy(psaDeclaration = None))

      result mustBe Left(ValidationError("PsaDeclaration missing from submission"))
    }

    "must return a Left of ValidationError when Psp submits with no Psp Declaration" in {
      val validator = new SubmissionValidatorImpl()

      val result = validator.validate(samplePspSubmission.copy(pspDeclaration = None))

      result mustBe Left(ValidationError("PspDeclaration missing from submission"))
    }

    "must return a Left of ValidationError when Psp submits with no PsaId in QtDeclaration" in {
      val validator = new SubmissionValidatorImpl()

      val result = validator.validate(samplePspSubmission.copy(qtDeclaration = QtDeclaration(Psp, PspId("12345678"), None)))

      result mustBe Left(ValidationError("Authorising PsaId is missing from Submission"))
    }

    "must return a Left of ValidationError when Psa submits with declaration1 = false" in {
      val validator = new SubmissionValidatorImpl()

      val result = validator.validate(samplePsaSubmission.copy(psaDeclaration = Some(Declaration(declaration1 = false, declaration2 = true))))

      result mustBe Left(ValidationError("Submission payload is invalid"))
    }

    "must return a Left of ValidationError when Psa submits with declaration2 = false" in {
      val validator = new SubmissionValidatorImpl()

      val result = validator.validate(samplePsaSubmission.copy(psaDeclaration = Some(Declaration(declaration1 = true, declaration2 = false))))

      result mustBe Left(ValidationError("Submission payload is invalid"))
    }

    "must return a Left of ValidationError when Psp submits with declaration1 = false" in {
      val validator = new SubmissionValidatorImpl()

      val result = validator.validate(samplePspSubmission.copy(pspDeclaration = Some(Declaration(declaration1 = false, declaration2 = true))))

      result mustBe Left(ValidationError("Submission payload is invalid"))
    }

    "must return a Left of ValidationError when Psp submits with declaration2 = false" in {
      val validator = new SubmissionValidatorImpl()

      val result = validator.validate(samplePspSubmission.copy(pspDeclaration = Some(Declaration(declaration1 = true, declaration2 = false))))

      result mustBe Left(ValidationError("Submission payload is invalid"))
    }
  }
}
