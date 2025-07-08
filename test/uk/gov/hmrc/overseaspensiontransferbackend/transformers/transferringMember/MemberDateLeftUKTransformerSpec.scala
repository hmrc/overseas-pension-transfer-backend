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

class MemberDateLeftUKTransformerSpec extends AnyWordSpec with Matchers {

  private val transformer = new MemberDateLeftUKTransformer

  "MemberDateLeftUKTransformer" should {

    "construct: move memberDetails.dateMemberLeftUk to transferringMember.memberDetails.memberResidencyDetails.lastPrincipalAddDetails.dateMemberLeftUk" in {
      val inputJson = Json.obj(
        "memberDetails" -> Json.obj(
          "dateMemberLeftUk" -> "2020-06-30"
        )
      )

      val expected = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "memberResidencyDetails" -> Json.obj(
              "lastPrincipalAddDetails" -> Json.obj(
                "dateMemberLeftUk" -> "2020-06-30"
              )
            )
          )
        )
      )

      val result = transformer.construct(inputJson)
      result shouldBe Right(expected)
    }

    "deconstruct: move transferringMember.memberDetails.memberResidencyDetails.lastPrincipalAddDetails.dateMemberLeftUk to memberDetails.dateMemberLeftUk" in {
      val inputJson = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "memberResidencyDetails" -> Json.obj(
              "lastPrincipalAddDetails" -> Json.obj(
                "dateMemberLeftUk" -> "2020-06-30"
              )
            )
          )
        )
      )

      val expected = Json.obj(
        "memberDetails" -> Json.obj(
          "dateMemberLeftUk" -> "2020-06-30"
        )
      )

      val result = transformer.deconstruct(inputJson)
      result shouldBe Right(expected)
    }

    "construct: leave JSON unchanged if memberDetails.dateMemberLeftUk is missing" in {
      val inputJson = Json.obj(
        "memberDetails" -> Json.obj()
      )

      val result = transformer.construct(inputJson)
      result shouldBe Right(inputJson)
    }

    "deconstruct: leave JSON unchanged if transferringMember.memberDetails.memberResidencyDetails.lastPrincipalAddDetails.dateMemberLeftUk is missing" in {
      val inputJson = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "memberResidencyDetails" -> Json.obj(
              "lastPrincipalAddDetails" -> Json.obj()
            )
          )
        )
      )

      val result = transformer.deconstruct(inputJson)
      result shouldBe Right(inputJson)
    }
  }
}
