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

package uk.gov.hmrc.overseaspensiontransferbackend.models.dtos

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.overseaspensiontransferbackend.models.Pstr

import java.time.Instant

class UserAnswersDTOSpec extends AnyFreeSpec with Matchers {

  "UserAnswersDTO" - {

    "must serialize and deserialize correctly" in {
      val dto = UserAnswersDTO(
        referenceId = "user-123",
        pstr        = Pstr("12345678AB"),
        data        = Json.obj("foo" -> "bar"),
        lastUpdated = Instant.parse("2025-04-11T12:00:00Z")
      )

      val json   = Json.toJson(dto)
      val result = json.validate[UserAnswersDTO]

      result mustBe JsSuccess(dto)
    }

    "must fail to deserialize if required fields are missing" in {
      val json = Json.obj("referenceId" -> "user-123")

      val result = json.validate[UserAnswersDTO]

      result.isError mustBe true
    }

    "must round-trip with an empty object for data" in {
      val dto    = UserAnswersDTO("abc", Pstr("12345678AB"), Json.obj(), Instant.now)
      val result = Json.toJson(dto).validate[UserAnswersDTO]

      result mustBe JsSuccess(dto)
    }
  }
}
