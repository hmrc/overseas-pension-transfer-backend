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
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication._
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{NormalisedSubmission, QtNumber, TransferNumber}
import uk.gov.hmrc.overseaspensiontransferbackend.models.{Declaration, QtDeclaration, ReportDetails, Submitted}

import java.util.UUID

class SubmissionSpec extends AnyFreeSpec with Matchers with SpecBase {

  "apply" - {
    "create submission object with no QtReference, no qtDeclaration.psaId and no pspDeclaration" in {
      val normalisedSubmission = NormalisedSubmission(
        TransferNumber(UUID.randomUUID().toString),
        PsaId("A1234567"),
        None,
        now,
        psaUser
      )

      val expected = Submission(
        ReportDetails(pstr.value, Submitted, None, None),
        None,
        None,
        None,
        QtDeclaration(
          Psa,
          PsaId("A1234567"),
          None
        ),
        Some(Declaration(true, declaration2 = true)),
        None
      )

      Submission.apply(simpleSavedUserAnswers, normalisedSubmission) mustBe expected
    }

    "create submission object with QtReference, qtDeclaration.psaId and pspDeclaration with no psaDeclaration" in {
      val normalisedSubmission = NormalisedSubmission(
        QtNumber("QT123456"),
        PspId("12345678"),
        Some(PsaId("A1234567")),
        now,
        psaUser
      )

      val expected = Submission(
        ReportDetails(pstr.value, Submitted, Some("QT123456"), None),
        None,
        None,
        None,
        QtDeclaration(
          Psp,
          PspId("12345678"),
          Some(PsaId("A1234567"))
        ),
        None,
        Some(Declaration(true, declaration2 = true))
      )

      Submission.apply(simpleSavedUserAnswers, normalisedSubmission) mustBe expected
    }

    "create submission object with no declaration when submitToHMRC is None" in {
      val normalisedSubmission = NormalisedSubmission(
        QtNumber("QT123456"),
        PspId("12345678"),
        Some(PsaId("A1234567")),
        now,
        psaUser
      )

      val expected = Submission(
        ReportDetails(pstr.value, Submitted, Some("QT123456"), None),
        None,
        None,
        None,
        QtDeclaration(
          Psp,
          PspId("12345678"),
          Some(PsaId("A1234567"))
        ),
        None,
        None
      )

      Submission.apply(simpleSavedUserAnswers.copy(data = sampleAnswersData.copy(submitToHMRC = None)), normalisedSubmission) mustBe expected
    }
  }
}
