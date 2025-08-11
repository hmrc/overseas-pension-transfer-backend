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

import org.scalatest.freespec.AnyFreeSpec
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission._

import scala.concurrent.Await
import scala.concurrent.duration._

class DummySubmissionServiceSpec extends AnyFreeSpec with SpecBase {

  "DummySubmissionService" - {
    "must return Right(SubmissionResponse) with qtNumber" in {
      val service    = new DummySubmissionServiceImpl()
      val submission = NormalisedSubmission(
        referenceId = "ref-ABC123",
        submitter   = Submitter.PsaSubmitter(PsaId("A1234567")),
        psaId       = PsaId("A1234567"),
        lastUpdated = now
      )

      val result = Await.result(service.submitAnswers(submission), 2.seconds)
      result mustBe Right(SubmissionResponse(QtNumber("QT123456")))
    }
  }
}
