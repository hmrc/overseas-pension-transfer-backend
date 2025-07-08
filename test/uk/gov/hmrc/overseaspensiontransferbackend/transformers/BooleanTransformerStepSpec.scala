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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class BooleanTransformerStepSpec extends AnyWordSpec with Matchers with BooleanTransformerStep {

  private val path = __ \ "memberDetails" \ "isUKResident"

  "BooleanTransformerStep" should {

    "constructBool: convert true to Yes string" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("isUKResident" -> true))

      val expected = Json.obj("memberDetails" -> Json.obj("isUKResident" -> "Yes"))

      constructBool(path)(inputJson) shouldBe Right(expected)
    }

    "constructBool: convert false to No string" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("isUKResident" -> false))

      val expected = Json.obj("memberDetails" -> Json.obj("isUKResident" -> "No"))

      constructBool(path)(inputJson) shouldBe Right(expected)
    }

    "constructBool: leave unchanged if value is not a boolean" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("isUKResident" -> "maybe"))

      constructBool(path)(inputJson) shouldBe Right(inputJson)
    }

    "constructBool: leave unchanged if path is missing" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj())

      constructBool(path)(inputJson) shouldBe Right(inputJson)
    }

    "deconstructBool: convert Yes string to true" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("isUKResident" -> "Yes"))

      val expected = Json.obj("memberDetails" -> Json.obj("isUKResident" -> true))

      deconstructBool(path)(inputJson) shouldBe Right(expected)
    }

    "deconstructBool: convert No string to false" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("isUKResident" -> "No"))

      val expected = Json.obj("memberDetails" -> Json.obj("isUKResident" -> false))

      deconstructBool(path)(inputJson) shouldBe Right(expected)
    }

    "deconstructBool: leave unchanged if value is not Yes or No" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("isUKResident" -> "maybe"))

      deconstructBool(path)(inputJson) shouldBe Right(inputJson)
    }

    "deconstructBool: leave unchanged if path is missing" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj())

      deconstructBool(path)(inputJson) shouldBe Right(inputJson)
    }
  }
}
