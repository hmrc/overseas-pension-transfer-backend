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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class MemberAddressTransformerSpec extends AnyWordSpec with Matchers {

  private val transformer = new MemberAddressTransformer

  "MemberAddressTransformer" should {

    "construct: move and wrap memberDetails.principalResAddDetails into addressDetails and poBox under transferringMember.memberDetails" in {
      val inputJson = Json.obj(
        "memberDetails" -> Json.obj(
          "principalResAddDetails" -> Json.obj(
            "addressLine1" -> "123 Test St",
            "addressLine2" -> "Testville",
            "addressLine3" -> "Testshire",
            "postcode"     -> "TE5 7ST",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom"),
            "poBox"        -> "PO123"
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
              "poBox"          -> "PO123"
            )
          )
        )
      )

      val result = transformer.construct(inputJson)
      result shouldBe Right(expected)
    }

    "deconstruct: unwrap transferringMember.memberDetails.principalResAddDetails from addressDetails and poBox back to memberDetails" in {
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
              "poBox"          -> "PO123"
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
            "postcode"     -> "TE5 7ST",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom"),
            "poBox"        -> "PO123"
          )
        )
      )

      val result = transformer.deconstruct(inputJson)
      result shouldBe Right(expected)
    }

    "construct: leave JSON unchanged if memberDetails.principalResAddDetails is missing" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj())

      val result = transformer.construct(inputJson)
      result shouldBe Right(Json.obj("memberDetails" -> Json.obj()))
    }

    "deconstruct: leave JSON unchanged if transferringMember.memberDetails.principalResAddDetails is missing" in {
      val inputJson = Json.obj(
        "transferringMember" -> Json.obj("memberDetails" -> Json.obj())
      )

      val result = transformer.deconstruct(inputJson)
      result shouldBe Right(Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj())))
    }
  }
}
