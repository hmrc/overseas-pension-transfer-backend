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
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.QtNumber

import java.time.Instant

class DownstreamSuccessSpec extends AnyFreeSpec with SpecBase {

  "DownstreamSuccess reads" - {

    "must read processingDate, formBundleNumber and qtReference from nested success object" in {
      val json =
        Json.parse("""
          {
            "success": {
              "processingDate": "2022-01-31T09:26:17Z",
              "formBundleNumber": "119000004320",
              "qtReference": "QT123456"
            }
          }
        """)

      val result = json.validate[DownstreamSuccess]

      result mustBe JsSuccess(
        DownstreamSuccess(
          qtNumber         = QtNumber("QT123456"),
          processingDate   = Instant.parse("2022-01-31T09:26:17Z"),
          formBundleNumber = "119000004320"
        )
      )
    }

    "must fail to read when required fields are missing" in {
      val json   = Json.parse("""{ "success": { "processingDate": "2022-01-31T09:26:17Z" } }""")
      val result = json.validate[DownstreamSuccess]
      result.isError mustBe true
    }
  }
}
