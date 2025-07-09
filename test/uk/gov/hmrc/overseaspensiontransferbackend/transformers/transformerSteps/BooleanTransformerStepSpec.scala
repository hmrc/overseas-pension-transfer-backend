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

class BooleanTransformerStepSpec extends AnyFreeSpec with Matchers with BooleanTransformerStep {

  private val path = __ \ "memberDetails" \ "isUKResident"

  "BooleanTransformerStep" - {

    "must convert true to Yes string" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("isUKResident" -> true))

      val expected = Json.obj("memberDetails" -> Json.obj("isUKResident" -> "Yes"))

      constructBool(path)(inputJson) mustBe Right(expected)
    }

    "must convert false to No string" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("isUKResident" -> false))

      val expected = Json.obj("memberDetails" -> Json.obj("isUKResident" -> "No"))

      constructBool(path)(inputJson) mustBe Right(expected)
    }

    "must leave unchanged if value is not a boolean" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("isUKResident" -> "maybe"))

      constructBool(path)(inputJson) mustBe Right(inputJson)
    }

    "must convert Yes string to true" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("isUKResident" -> "Yes"))

      val expected = Json.obj("memberDetails" -> Json.obj("isUKResident" -> true))

      deconstructBool(path)(inputJson) mustBe Right(expected)
    }

    "must convert No string to false" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("isUKResident" -> "No"))

      val expected = Json.obj("memberDetails" -> Json.obj("isUKResident" -> false))

      deconstructBool(path)(inputJson) mustBe Right(expected)
    }

    "must leave unchanged if value is not Yes or No" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("isUKResident" -> "maybe"))

      deconstructBool(path)(inputJson) mustBe Right(inputJson)
    }
  }
}
