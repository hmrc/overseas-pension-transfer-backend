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
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.TransferNumber
import uk.gov.hmrc.overseaspensiontransferbackend.models.{PstrNumber, SavedUserAnswers}

class SubmissionValidatorSpec extends AnyFreeSpec with SpecBase {

  "SubmissionValidatorImpl" - {

    "must return Right(ValidatedSubmission) when data contains reportDetails" in {
      val saved = SavedUserAnswers(
        transferId  = TransferNumber("ref-123"),
        pstr        = PstrNumber("12345678AB"),
        data        = sampleAnswersData,
        lastUpdated = now
      )

      val validator = new SubmissionValidatorImpl()

      val result = validator.validate(saved)

      result mustBe Right(ValidatedSubmission(saved))
    }

    "must return an Left of ValidationError when data is missing reportDetails" in {
      val saved = SavedUserAnswers(
        transferId  = TransferNumber("ref-123"),
        pstr        = PstrNumber("12345678AB"),
        data        = sampleAnswersData.copy(reportDetails = None),
        lastUpdated = now
      )

      val validator = new SubmissionValidatorImpl()

      val result = validator.validate(saved)

      result mustBe Left(ValidationError("Report Details missing invalid payload"))
    }
  }
}
