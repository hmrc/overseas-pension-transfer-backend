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
  }
}
