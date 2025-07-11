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
import org.scalatest.EitherValues
import play.api.libs.json._

class MemberNameTransformerSpec extends AnyFreeSpec with Matchers with EitherValues {

  private val transformer = new MemberNameTransformer

  "MemberNameTransformer" - {

    "must construct the correct structure with flattened name fields" in {
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

      transformer.construct(inputJson).value mustBe expected
    }

    "must deconstruct the flattened structure back to nested name fields" in {
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

      transformer.deconstruct(inputJson).value mustBe expected
    }

    "must return original JSON if name key not present on construct" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("nino" -> "AB123456A"))

      transformer.construct(inputJson).value mustBe inputJson
    }

    "must return original JSON if foreName/lastName not present on deconstruct" in {
      val inputJson = Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj("nino" -> "AB123456A")))

      transformer.deconstruct(inputJson).value mustBe inputJson
    }
  }
}
