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

class MemberNoNinoTransformerSpec extends AnyWordSpec with Matchers {

  val transformer = new MemberNoNinoTransformer

  "MemberNoNinoTransformer" should {

    "construct: move memberDetails.reasonNoNINO to transferringMember.memberDetails.reasonNoNINO" in {
      val inputJson = Json.obj(
        "memberDetails" -> Json.obj(
          "reasonNoNINO" -> "reason for no nino"
        )
      )

      val expected = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "reasonNoNINO" -> "reason for no nino"
          )
        )
      )

      val result = transformer.construct(inputJson)
      result shouldBe Right(expected)
    }

    "deconstruct: move transferringMember.memberDetails.reasonNoNINO to memberDetails.reasonNoNINO" in {
      val inputJson = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "reasonNoNINO" -> "reason for no nino"
          )
        )
      )

      val expected = Json.obj(
        "memberDetails" -> Json.obj(
          "reasonNoNINO" -> "reason for no nino"
        )
      )

      val result = transformer.deconstruct(inputJson)
      result shouldBe Right(expected)
    }

    "construct: leave JSON unchanged if memberDetails.reasonNoNINO is missing" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj())

      val result = transformer.construct(inputJson)
      result shouldBe Right(Json.obj("memberDetails" -> Json.obj()))
    }

    "deconstruct: leave JSON unchanged if transferringMember.memberDetails.reasonNoNINO is missing" in {
      val inputJson = Json.obj(
        "transferringMember" -> Json.obj("memberDetails" -> Json.obj())
      )

      val result = transformer.deconstruct(inputJson)
      result shouldBe Right(Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj())))
    }
  }
}
