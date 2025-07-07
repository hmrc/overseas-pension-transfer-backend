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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class MemberEverUKResidentTransformerSpec extends AnyWordSpec with Matchers {

  val transformer = new MemberEverUKResidentTransformer

  "MemberIsUKResidentTransformer" should {

    "construct: move and convert memberDetails.memEverUkResident boolean to nested Yes/No string" in {
      val input = Json.obj(
        "memberDetails" -> Json.obj(
          "memEverUkResident" -> true
        )
      )

      val expected = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "memberResidencyDetails" -> Json.obj(
              "memEverUkResident" -> "Yes"
            )
          )
        )
      )

      transformer.construct(input) mustBe Right(expected)
    }

    "deconstruct: move and convert nested Yes/No string back to boolean" in {
      val input = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "memberResidencyDetails" -> Json.obj(
              "memEverUkResident" -> "No"
            )
          )
        )
      )

      val expected = Json.obj(
        "memberDetails" -> Json.obj(
          "memEverUkResident" -> false
        )
      )

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "construct: leave JSON unchanged if memEverUkResident is missing" in {
      val input = Json.obj(
        "memberDetails" -> Json.obj()
      )

      transformer.construct(input) mustBe Right(input)
    }

    "deconstruct: leave JSON unchanged if memEverUkResident is missing in nested structure" in {
      val input = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "memberResidencyDetails" -> Json.obj()
          )
        )
      )

      transformer.deconstruct(input) mustBe Right(input)
    }

  }
}
