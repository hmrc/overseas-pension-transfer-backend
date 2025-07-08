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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class AddressTransformerStepSpec extends AnyFreeSpec with Matchers with AddressTransformerStep {

  "constructAddressAt" - {
    "should convert flat frontend-style address to backend-style nested structure" in {
      val input = Json.obj(
        "principalResAddRetails" -> Json.obj(
          "addressLine1" -> "123 Test St",
          "addressLine2" -> "Testville",
          "addressLine3" -> "Testshire",
          "ukPostCode"   -> "TE5 7ST",
          "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom"),
          "poBoxNumber"  -> "PO123"
        )
      )

      val expected = Json.obj(
        "principalResAddRetails" -> Json.obj(
          "addressDetails" -> Json.obj(
            "addressLine1" -> "123 Test St",
            "addressLine2" -> "Testville",
            "addressLine3" -> "Testshire",
            "ukPostCode"   -> "TE5 7ST",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "poBoxNumber"    -> "PO123"
        )
      )

      constructAddressAt(__ \ "principalResAddRetails", nestedKey = "addressDetails")(input) mustBe Right(expected)
    }
  }

  "deconstructAddressAt" - {
    "should convert nested backend-style address back to flat frontend-style structure" in {
      val input = Json.obj(
        "principalResAddDetails" -> Json.obj(
          "addressDetails" -> Json.obj(
            "addressLine1" -> "123 Test St",
            "addressLine2" -> "Testville",
            "addressLine3" -> "Testshire",
            "ukPostCode"   -> "TE5 7ST",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          ),
          "poBoxNumber"    -> "PO123"
        )
      )

      val expected = Json.obj(
        "principalResAddDetails" -> Json.obj(
          "addressLine1" -> "123 Test St",
          "addressLine2" -> "Testville",
          "addressLine3" -> "Testshire",
          "ukPostCode"   -> "TE5 7ST",
          "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom"),
          "poBoxNumber"  -> "PO123"
        )
      )

      deconstructAddressAt(__ \ "principalResAddDetails", nestedKey = "addressDetails")(input) mustBe Right(expected)
    }
  }
}
