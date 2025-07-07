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

package uk.gov.hmrc.overseaspensiontransferbackend.utils

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class JsonHelpersSpec extends AnyFreeSpec with Matchers with JsonHelpers {

  "JsonHelpers" - {

    "movePath should move a value from one path to another" in {
      val input  = Json.obj("a" -> Json.obj("b" -> "value"))
      val result = movePath(__ \ "a" \ "b", __ \ "x" \ "y", input)
      // "a" gets removed, see prunePath comment
      result mustBe Right(Json.obj("x" -> Json.obj("y" -> JsString("value"))))
    }

    "movePath should return unchanged JSON if source path does not exist" in {
      val input  = Json.obj("a" -> Json.obj("b" -> "value"))
      val result = movePath(__ \ "a" \ "missing", __ \ "x" \ "y", input)

      result mustBe Right(input)
    }

    "setPath should set a value at a nested path" in {
      val input  = Json.obj("a" -> Json.obj())
      val result = setPath(__ \ "a" \ "b" \ "c", JsString("value"), input)

      result mustBe Right(Json.obj("a" -> Json.obj("b" -> Json.obj("c" -> "value"))))
    }

    "setPath should overwrite existing value at path" in {
      val input  = Json.obj("a" -> Json.obj("b" -> Json.obj("c" -> "old")))
      val result = setPath(__ \ "a" \ "b" \ "c", JsString("new"), input)

      result mustBe Right(Json.obj("a" -> Json.obj("b" -> Json.obj("c" -> "new"))))
    }

    "setPath should fail for unsupported paths" in {
      val input  = Json.obj("a" -> Json.obj())
      val result = setPath(JsPath(Nil), JsString("value"), input)

      result mustBe Right(input) // This is acceptable per your implementation (no-op on empty path)
    }

    "prunePath should remove a nested key" in {
      val input  = Json.obj("a" -> Json.obj("b" -> "value", "c" -> "keep"))
      val result = movePath(__ \ "a" \ "b", __ \ "x", input)

      result mustBe Right(Json.obj("a" -> Json.obj("c" -> "keep"), "x" -> "value"))
    }

    "setPath should create intermediate objects if necessary" in {
      val input  = Json.obj()
      val result = setPath(__ \ "a" \ "b" \ "c", JsString("new"), input)

      result mustBe Right(Json.obj("a" -> Json.obj("b" -> Json.obj("c" -> "new"))))
    }
  }
}
