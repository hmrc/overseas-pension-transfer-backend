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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class TransferDetailsSpec extends AnyFreeSpec with Matchers {

  "TransferDetails" - {

    "must serialize and deserialize correctly with value" in {
      val typeOfAssets = TypeOfAssets(Some("Yes"), None, None, None, None, None, None, None, None, None, None, None, None)
      val model        = TransferDetails(
        Some(12345.67),
        Some(54321.99),
        Some(LocalDate.of(2012, 12, 12)),
        Some("No"),
        Some("Yes"),
        Some(TaxableOverseasTransferDetails(Some(Occupational), Some(12345.99), Some(54321.99))),
        Some(typeOfAssets)
      )
      val json         = Json.toJson(model)
      val result       = json.validate[TransferDetails]

      result.get mustBe model
    }

    "must deserialize with missing field as None" in {
      val json   = Json.obj()
      val result = json.validate[TransferDetails]

      result mustBe JsSuccess(TransferDetails(None, None, None, None, None, None, None))
    }
  }
}
