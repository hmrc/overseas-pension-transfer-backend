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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class NameTransformerStepSpec extends AnyFreeSpec with Matchers with NameTransformerStep {

  "NameTransformerStep" - {
    "must flatten name with firstName and lastName to foreName and lastName" in {
      val input    = Json.obj("memberDetails" -> Json.obj("name" -> Json.obj("firstName" -> "Alice", "lastName" -> "Smith")))
      val expected = Json.obj("memberDetails" -> Json.obj("foreName" -> "Alice", "lastName" -> "Smith"))

      flattenName(__ \ "memberDetails" \ "name")(input) mustBe Right(expected)
    }

    "must flatten name with firstName and lastName to foreName and lastName and leave all other fields untouched" in {
      val input    = Json.obj("memberDetails" -> Json.obj("name" -> Json.obj("firstName" -> "Alice", "lastName" -> "Smith"), "otherField" -> "leave"))
      val expected = Json.obj("memberDetails" -> Json.obj("foreName" -> "Alice", "lastName" -> "Smith", "otherField" -> "leave"))

      flattenName(__ \ "memberDetails" \ "name")(input) mustBe Right(expected)
    }

    "must flatten name with only firstName" in {
      val input    = Json.obj("memberDetails" -> Json.obj("name" -> Json.obj("firstName" -> "Alice")))
      val expected = Json.obj("memberDetails" -> Json.obj("foreName" -> "Alice"))

      flattenName(__ \ "memberDetails" \ "name")(input) mustBe Right(expected)
    }

    "must flatten name with only lastName" in {
      val input    = Json.obj("memberDetails" -> Json.obj("name" -> Json.obj("lastName" -> "Smith")))
      val expected = Json.obj("memberDetails" -> Json.obj("lastName" -> "Smith"))

      flattenName(__ \ "memberDetails" \ "name")(input) mustBe Right(expected)
    }

    "must return original JSON if path is missing" in {
      val input = Json.obj("memberDetails" -> Json.obj("nino" -> "AB123456C"))
      flattenName(__ \ "memberDetails" \ "name")(input) mustBe Right(input)
    }

    "must use custom keys if provided" in {
      val input    = Json.obj("memberDetails" -> Json.obj("name" -> Json.obj("firstName" -> "Alice", "lastName" -> "Smith")))
      val expected = Json.obj("memberDetails" -> Json.obj("given" -> "Alice", "surname" -> "Smith"))

      flattenName(__ \ "memberDetails" \ "name", foreNameKey = "given", lastNameKey = "surname")(input) mustBe Right(expected)
    }
    "must reconstruct name field from foreName and lastName" in {
      val input    = Json.obj("memberDetails" -> Json.obj("foreName" -> "Alice", "lastName" -> "Smith"))
      val expected = Json.obj("memberDetails" -> Json.obj("name" -> Json.obj("firstName" -> "Alice", "lastName" -> "Smith")))

      unflattenName(path = __ \ "memberDetails" \ "name")(input) mustBe Right(expected)
    }

    "must return original JSON if neither name part exists" in {
      val input = Json.obj("memberDetails" -> Json.obj("nino" -> "AB123456C"))
      unflattenName(path = __ \ "memberDetails" \ "name")(input) mustBe Right(input)
    }

    "must reconstruct name from only foreName" in {
      val input    = Json.obj("memberDetails" -> Json.obj("foreName" -> "Alice"))
      val expected = Json.obj("memberDetails" -> Json.obj("name" -> Json.obj("firstName" -> "Alice")))

      unflattenName(path = __ \ "memberDetails" \ "name")(input) mustBe Right(expected)
    }

    "must reconstruct name from only lastName" in {
      val input    = Json.obj("memberDetails" -> Json.obj("lastName" -> "Smith"))
      val expected = Json.obj("memberDetails" -> Json.obj("name" -> Json.obj("lastName" -> "Smith")))

      unflattenName(path = __ \ "memberDetails" \ "name")(input) mustBe Right(expected)
    }

    "must support custom keys" in {
      val input    = Json.obj("memberDetails" -> Json.obj("given" -> "Alice", "surname" -> "Smith"))
      val expected = Json.obj("memberDetails" -> Json.obj("name" -> Json.obj("firstName" -> "Alice", "lastName" -> "Smith")))

      unflattenName(foreNameKey = "given", lastNameKey = "surname", path = __ \ "memberDetails" \ "name")(input) mustBe Right(expected)
    }
  }
}
