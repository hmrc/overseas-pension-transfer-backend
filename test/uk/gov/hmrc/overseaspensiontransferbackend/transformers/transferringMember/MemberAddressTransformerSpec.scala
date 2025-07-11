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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferringMember

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json._

class MemberAddressTransformerSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  private val transformer = new MemberAddressTransformer

  "MemberAddressTransformer" - {

    "must move and wrap memberDetails.principalResAddDetails into addressDetails and poBoxNumber under transferringMember.memberDetails" in {
      val inputJson = Json.obj(
        "memberDetails" -> Json.obj(
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "123 Test St",
            "addressLine2" -> "Testville",
            "addressLine3" -> "Testshire",
            "ukPostCode"   -> "TE5 7ST",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom"),
            "poBoxNumber"  -> "PO123"
          )
        )
      )

      val expected = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
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
        )
      )

      val result = transformer.construct(inputJson)
      result mustBe Right(expected)
    }

    "must unwrap transferringMember.memberDetails.principalResAddDetails from addressDetails and poBoxNumber back to memberDetails" in {
      val inputJson = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
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
        )
      )

      val expected = Json.obj(
        "memberDetails" -> Json.obj(
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "123 Test St",
            "addressLine2" -> "Testville",
            "addressLine3" -> "Testshire",
            "ukPostCode"   -> "TE5 7ST",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom"),
            "poBoxNumber"  -> "PO123"
          )
        )
      )

      val result = transformer.deconstruct(inputJson)
      result mustBe Right(expected)
    }

    "must leave JSON unchanged if memberDetails.principalResAddDetails is missing" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj())

      val result = transformer.construct(inputJson)
      result mustBe Right(Json.obj("memberDetails" -> Json.obj()))
    }

    "must leave JSON unchanged if transferringMember.memberDetails.principalResAddDetails is missing" in {
      val inputJson = Json.obj(
        "transferringMember" -> Json.obj("memberDetails" -> Json.obj())
      )

      val result = transformer.deconstruct(inputJson)
      result mustBe Right(Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj())))
    }
  }
}
