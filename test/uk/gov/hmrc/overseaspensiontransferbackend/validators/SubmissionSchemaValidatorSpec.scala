/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.overseaspensiontransferbackend.models.*
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication.{Psa, PsaId}

class SubmissionSchemaValidatorSpec extends AnyWordSpec with Matchers {

  val schemaValidator = new SubmissionSchemaValidator

  "SubmissionSchemaValidator.validate" should {

    "return no schema errors for a valid submission payload" in {
      val submission = Submission(
        reportDetails       = ReportDetails(
          pstr            = "24000001IN",
          qtStatus        = Submitted,
          qtReference     = Some("QT123456"),
          qtDigitalStatus = None
        ),
        transferringMember  = None,
        aboutReceivingQROPS = None,
        transferDetails     = None,
        qtDeclaration       = QtDeclaration(
          submittedBy = Psa,
          submitterId = PsaId("A1234567"),
          psaId       = Some(PsaId("A1234567"))
        ),
        psaDeclaration      = Some(Declaration(true, true)),
        pspDeclaration      = None
      )

      val result = schemaValidator.validate(Json.toJson(submission))

      result shouldBe empty
    }

    "return schema errors for an invalid payload" in {
      val invalidPayload = Json.obj(
        "reportDetails" -> Json.obj(
          "pstr" -> 123
        )
      )

      val result = schemaValidator.validate(invalidPayload)

      result should not be empty
    }
  }
}
