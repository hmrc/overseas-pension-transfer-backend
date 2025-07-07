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

import play.api.libs.json._

object JsonHelpers {

  def movePath(from: JsPath, to: JsPath, json: JsObject): Either[JsError, JsObject] = {
    from.asSingleJson(json).toOption match {
      case Some(value) =>
        // Remove old path
        val withoutOld = prunePath(from, json)
        // Set value at new path
        setPath(to, value, withoutOld)
      case None        =>
        Right(json) // Nothing to move; return as-is
    }
  }

  def setPath(path: JsPath, value: JsValue, json: JsObject): Either[JsError, JsObject] = {
    path.json.prune andThen
      path.json.put(value)
    Json.obj(path.path.lastOption.map(_.toString).getOrElse("unknown") -> value) // fallback

    setNested(json, path, value)
  }

  private def prunePath(path: JsPath, json: JsObject): JsObject = {
    path.path match {
      case Seq(KeyPathNode(key))             =>
        json - key
      case Seq(KeyPathNode(head), tail @ _*) =>
        val nested = json.value.get(head).collect { case o: JsObject => o }
        nested match {
          case Some(inner) =>
            val cleaned = prunePath(JsPath(tail.toList), inner)
            json + (head -> cleaned)
          case None        => json
        }
      case _                                 => json
    }
  }

  private def setNested(json: JsObject, path: JsPath, value: JsValue): Either[JsError, JsObject] = {
    path.path match {
      case Nil                               => Right(json)
      case Seq(KeyPathNode(key))             =>
        Right(json + (key -> value))
      case Seq(KeyPathNode(head), tail @ _*) =>
        val remaining = JsPath(tail.toList)
        val nestedObj = json.value.get(head).collect {
          case obj: JsObject => obj
        }.getOrElse(Json.obj())

        setNested(nestedObj, remaining, value).map(nested =>
          json + (head -> nested)
        )
      case _                                 => Left(JsError(s"Unsupported path: $path"))
    }
  }
}
