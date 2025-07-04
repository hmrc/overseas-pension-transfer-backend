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

package uk.gov.hmrc.overseaspensiontransferbackend.models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

import java.time.Instant

class SavedUserAnswersSpec extends AnyWordSpec with Matchers {

  "SavedUserAnswers JSON format" should {

    "round trip to and from JSON" in {
      val original = SavedUserAnswers(
        referenceId = "ref-123",
        data        = AnswersData(
          transferringMember   = Some(TransferringMember(None)),
          qropsDetails         = None,
          schemeManagerDetails = None,
          transferDetails      = None
        ),
        lastUpdated = Instant.parse("2024-01-01T12:00:00Z")
      )

      val json   = Json.toJson(original)
      val parsed = json.validate[SavedUserAnswers]

      parsed mustBe JsSuccess(original)
    }

    "flatten AnswersData to JsObject in writes" in {
      val data = AnswersData(
        transferringMember   = Some(TransferringMember(Some(MemberDetails(foreName = Some("Jane"))))),
        qropsDetails         = None,
        schemeManagerDetails = None,
        transferDetails      = None
      )

      val obj = SavedUserAnswers("ref-456", data, Instant.parse("2025-01-01T10:00:00Z"))

      val json = Json.toJson(obj)

      (json \ "data" \ "transferringMember" \ "memberDetails" \ "foreName").as[String] mustBe "Jane"
      (json \ "referenceId").as[String] mustBe "ref-456"
    }
  }
}
