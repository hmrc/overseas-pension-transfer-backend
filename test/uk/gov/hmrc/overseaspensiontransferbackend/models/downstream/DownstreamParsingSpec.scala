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

package uk.gov.hmrc.overseaspensiontransferbackend.models.downstream

import org.scalatest.freespec.AnyFreeSpec
import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase

class DownstreamParsingSpec extends AnyFreeSpec with SpecBase {

  "Downstream parsing" - {

    "must parse HIP bad request (responseSystemErrorType)" in {
      val json = Json.parse(
        """
          |{
          |  "origin": "HoD",
          |  "response": {
          |    "error": {
          |      "code": "400",
          |      "logID": "ABCDEF0123456789ABCDEF0123456789",
          |      "message": "Invalid JSON"
          |    }
          |  }
          |}
          |""".stripMargin
      )

      val expected = HipBadRequest(
        origin  = "HoD",
        code    = "400",
        message = "Invalid JSON",
        logId   = Some("ABCDEF0123456789ABCDEF0123456789")
      )

      val result = json.validate[HipBadRequest]
      result mustBe JsSuccess(expected)
    }

    "must parse HIP-originResponse failures array" in {
      val json = Json.parse(
        """
          |{
          |  "origin":"HIP",
          |  "response": {
          |    "failures": [
          |      { "type":"Type A", "reason":"Reason A" },
          |      { "type":"Type B", "reason":"Reason B" }
          |    ]
          |  }
          |}
          |""".stripMargin
      )

      val expected = HipOriginFailures(
        origin   = "HIP",
        failures = List(
          HipOriginFailures.Failure("Type A", "Reason A"),
          HipOriginFailures.Failure("Type B", "Reason B")
        )
      )

      val result = json.validate[HipOriginFailures]
      result mustBe JsSuccess(expected)
    }

    "must parse ETMP 422 business validation error" in {
      val json = Json.parse(
        """
          |{
          |  "errors": {
          |    "processingDate": "2025-07-01T09:30:00Z",
          |    "code": "003",
          |    "text": "Request could not be processed"
          |  }
          |}
          |""".stripMargin
      )

      val expected = EtmpValidationError(
        processingDate = "2025-07-01T09:30:00Z",
        code           = "003",
        text           = "Request could not be processed"
      )

      val result = json.validate[EtmpValidationError]
      result mustBe JsSuccess(expected)
    }
  }
}
