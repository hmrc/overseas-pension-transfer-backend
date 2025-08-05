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
import play.api.libs.json._

class MemberLastUKAddressTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer = new MemberLastUKAddressTransformer

  "MemberLastUKAddressTransformer" - {

    "must move and wrap memberDetails.lastPrincipalAddDetails into addressDetails and poBoxNumber under" +
      " transferringMember.memberDetails.memberResidencyDetails" in {
        val inputJson = Json.obj(
          "memberDetails" -> Json.obj(
            "lastPrincipalAddDetails" -> Json.obj(
              "addressLine1" -> "321 Old St",
              "addressLine2" -> "Oldtown",
              "ukPostCode"   -> "OL9 4LD",
              "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
            )
          )
        )

        val expected = Json.obj(
          "transferringMember" -> Json.obj(
            "memberDetails" -> Json.obj(
              "memberResidencyDetails" -> Json.obj(
                "lastPrincipalAddDetails" -> Json.obj(
                  "addressDetails" -> Json.obj(
                    "addressLine1" -> "321 Old St",
                    "addressLine2" -> "Oldtown",
                    "ukPostCode"   -> "OL9 4LD",
                    "country"      -> "GB"
                  )
                )
              )
            )
          )
        )

        transformer.construct(inputJson) mustBe Right(expected)
      }

    "must unwrap transferringMember.memberDetails.memberResidencyDetails.lastPrincipalAddDetails back to memberDetails.lastPrincipalAddDetails" in {
      val inputJson = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "memberResidencyDetails" -> Json.obj(
              "lastPrincipalAddDetails" -> Json.obj(
                "addressDetails" -> Json.obj(
                  "addressLine1" -> "321 Old St",
                  "addressLine2" -> "Oldtown",
                  "ukPostCode"   -> "OL9 4LD",
                  "country"      -> "GB"
                )
              )
            )
          )
        )
      )

      val expected = Json.obj(
        "memberDetails" -> Json.obj(
          "lastPrincipalAddDetails" -> Json.obj(
            "addressLine1" -> "321 Old St",
            "addressLine2" -> "Oldtown",
            "ukPostCode"   -> "OL9 4LD",
            "country"      -> "GB"
          )
        )
      )

      transformer.deconstruct(inputJson) mustBe Right(expected)
    }

    "must leave JSON unchanged if memberDetails.lastPrincipalAddDetails is missing" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj())

      transformer.construct(inputJson) mustBe Right(inputJson)
    }

    "must leave JSON unchanged if transferringMember.memberDetails.memberResidencyDetails.lastPrincipalAddDetails is missing" in {
      val inputJson = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "memberResidencyDetails" -> Json.obj()
          )
        )
      )

      transformer.deconstruct(inputJson) mustBe Right(inputJson)
    }
  }
}
