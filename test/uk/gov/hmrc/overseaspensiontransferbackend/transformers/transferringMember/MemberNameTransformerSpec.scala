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

import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class MemberNameTransformerSpec extends AnyWordSpec with Matchers with EitherValues {

  val transformer = new MemberNameTransformer

  "MemberNameTransformer" should {

    "construct the correct structure with flattened name fields" in {
      val inputJson = Json.obj(
        "memberDetails" -> Json.obj(
          "name" -> Json.obj(
            "firstName" -> "Mathew",
            "lastName"  -> "May"
          )
        )
      )

      val expected = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "foreName" -> "Mathew",
            "lastName" -> "May"
          )
        )
      )

      val result = transformer.construct(inputJson)
      result.value shouldBe expected
    }

    "deconstruct the flattened structure back to nested name fields" in {
      val inputJson = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "foreName" -> "Mathew",
            "lastName" -> "May"
          )
        )
      )

      val expected = Json.obj(
        "memberDetails" -> Json.obj(
          "name" -> Json.obj(
            "firstName" -> "Mathew",
            "lastName"  -> "May"
          )
        )
      )

      val result = transformer.deconstruct(inputJson)
      result.value shouldBe expected
    }

    "return original JSON if name key not present on construct" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("nino" -> "AB123456A"))

      val result = transformer.construct(inputJson)
      result.value shouldBe inputJson
    }

    "return original JSON if foreName/lastName not present on deconstruct" in {
      val inputJson = Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj("nino" -> "AB123456A")))

      val result = transformer.deconstruct(inputJson)
      result.value shouldBe inputJson
    }
  }
}
