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

    "movePath" - {

      "must move a value from one path to another and prune empty parents" in {
        val input  = Json.obj("a" -> Json.obj("b" -> "value"))
        val result = movePath(__ \ "a" \ "b", __ \ "x" \ "y", input)
        result mustBe Right(Json.obj("x" -> Json.obj("y" -> "value")))
      }

      "must return unchanged JSON if source path does not exist" in {
        val input  = Json.obj("a" -> Json.obj("b" -> "value"))
        val result = movePath(__ \ "a" \ "missing", __ \ "x" \ "y", input)
        result mustBe Right(input)
      }

      "must remove the original key and prune empty parents" in {
        val input  = Json.obj("a" -> Json.obj("b" -> "value", "c" -> "keep"))
        val result = movePath(__ \ "a" \ "b", __ \ "x", input)
        result mustBe Right(Json.obj("a" -> Json.obj("c" -> "keep"), "x" -> "value"))
      }
    }

    "setPath" - {

      "must set a value at a nested path" in {
        val input  = Json.obj("a" -> Json.obj())
        val result = setPath(__ \ "a" \ "b" \ "c", JsString("value"), input)
        result mustBe Right(Json.obj("a" -> Json.obj("b" -> Json.obj("c" -> "value"))))
      }

      "must overwrite an existing value at path" in {
        val input  = Json.obj("a" -> Json.obj("b" -> Json.obj("c" -> "old")))
        val result = setPath(__ \ "a" \ "b" \ "c", JsString("new"), input)
        result mustBe Right(Json.obj("a" -> Json.obj("b" -> Json.obj("c" -> "new"))))
      }

      "must prune the path if setting an empty object" in {
        val input  = Json.obj("a" -> Json.obj("b" -> Json.obj("c" -> "something")))
        val result = setPath(__ \ "a" \ "b" \ "c", Json.obj(), input)
        // Should remove "c" and also prune "b" and "a" since they are empty after
        result mustBe Right(Json.obj())
      }

      "must return unchanged JSON if path is empty" in {
        val input  = Json.obj("a" -> Json.obj("b" -> "value"))
        val result = setPath(JsPath(Nil), JsString("value"), input)
        result mustBe Right(input)
      }

      "must create intermediate objects if necessary" in {
        val input  = Json.obj()
        val result = setPath(__ \ "a" \ "b" \ "c", JsString("new"), input)
        result mustBe Right(Json.obj("a" -> Json.obj("b" -> Json.obj("c" -> "new"))))
      }
    }

    "prunePath" - {

      "must remove a nested key and prune empty parents" in {
        val input  = Json.obj("a" -> Json.obj("b" -> "value"))
        val result = prunePath(__ \ "a" \ "b")(input)
        result mustBe Json.obj()
      }

      "must remove a key but preserve non-empty parents" in {
        val input  = Json.obj("a" -> Json.obj("b" -> "value", "c" -> "keep"))
        val result = prunePath(__ \ "a" \ "b")(input)
        result mustBe Json.obj("a" -> Json.obj("c" -> "keep"))
      }

      "must do nothing if path does not exist" in {
        val input  = Json.obj("a" -> Json.obj("b" -> "value"))
        val result = prunePath(__ \ "a" \ "missing")(input)
        result mustBe input
      }
    }

    "pruneAndMerge" - {

      "must delete missing nested fields inside a touched top-level section and keep provided fields" in {
        val existing =
          Json.obj(
            "transferringMember" -> Json.obj(
              "memberDetails" -> Json.obj(
                "foreName" -> "Old",
                "lastName" -> "Name",
                "nino"     -> "AA123456A"
              ),
              "otherKey"      -> "keepMe"
            ),
            "reportDetails"      -> Json.obj("foo" -> "bar")
          )

        val update =
          Json.obj(
            "transferringMember" -> Json.obj(
              "memberDetails" -> Json.obj(
                "foreName" -> "New",
                "lastName" -> "Name"
              ),
              "otherKey"      -> "keepMe"
            ),
            "reportDetails"      -> Json.obj("foo" -> "bar")
          )

        val result = pruneAndMerge(existing.as[JsObject], update.as[JsObject])

        (result \ "transferringMember" \ "memberDetails" \ "foreName").as[String] mustBe "New"
        (result \ "transferringMember" \ "memberDetails" \ "lastName").as[String] mustBe "Name"
        (result \ "transferringMember" \ "memberDetails" \ "nino").toOption       mustBe None

        (result \ "reportDetails" \ "foo").as[String] mustBe "bar"

        (result \ "transferringMember" \ "otherKey").as[String] mustBe "keepMe"
      }

      "must replace arrays wholesale when present in the update" in {
        val existing =
          Json.obj(
            "transferDetails" -> Json.obj(
              "unquotedShares" -> Json.arr(
                Json.obj("amount" -> 100),
                Json.obj("amount" -> 200)
              )
            )
          )

        val update =
          Json.obj(
            "transferDetails" -> Json.obj(
              "unquotedShares" -> Json.arr(Json.obj("amount" -> 999))
            )
          )

        val result = pruneAndMerge(existing.as[JsObject], update.as[JsObject])

        val arr = (result \ "transferDetails" \ "unquotedShares").as[JsArray]
        arr.value                             must have size 1
        (arr.value.head \ "amount").as[Int] mustBe 999
      }

      "must clear a subtree if the update provides an empty object for that top-level section" in {
        val existing =
          Json.obj(
            "transferringMember" -> Json.obj(
              "memberDetails" -> Json.obj("foreName" -> "Old", "lastName" -> "Name")
            ),
            "reportDetails"      -> Json.obj("foo" -> "bar")
          )

        val update =
          Json.obj(
            "transferringMember" -> Json.obj()
          )

        val result = pruneAndMerge(existing.as[JsObject], update.as[JsObject])

        (result \ "transferringMember" \ "memberDetails").toOption mustBe None

        (result \ "reportDetails" \ "foo").as[String] mustBe "bar"
      }

      "must be a no-op if update is empty" in {
        val existing =
          Json.obj(
            "reportDetails"      -> Json.obj("foo" -> "bar"),
            "transferringMember" -> Json.obj(
              "memberDetails" -> Json.obj("foreName" -> "Old")
            )
          )

        val update = Json.obj()

        val result = pruneAndMerge(existing.as[JsObject], update.as[JsObject])

        result mustBe existing
      }

      "must delete deeply nested keys across multiple levels" in {
        val existing =
          Json.obj(
            "a"     -> Json.obj(
              "b" -> Json.obj(
                "c1" -> Json.obj(
                  "d1" -> "keep",
                  "d2" -> "remove"
                ),
                "c2" -> "keepC2"
              ),
              "x" -> "keepX"
            ),
            "other" -> "untouched"
          )

        val update =
          Json.obj(
            "a" -> Json.obj(
              "b" -> Json.obj(
                "c1" -> Json.obj(
                  "d1" -> "keep"
                )
              )
            )
          )

        val result = pruneAndMerge(existing.as[JsObject], update.as[JsObject])

        (result \ "a" \ "b" \ "c1" \ "d1").as[String] mustBe "keep"

        (result \ "a" \ "b" \ "c1" \ "d2").toOption mustBe None
        (result \ "a" \ "b" \ "c2").toOption        mustBe None
        (result \ "a" \ "x").toOption               mustBe None

        (result \ "other").as[String] mustBe "untouched"
      }
    }
  }
}
