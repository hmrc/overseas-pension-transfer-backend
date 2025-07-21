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
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.steps.TransformerStep

trait JsonHelpers {

  def movePath(from: JsPath, to: JsPath): TransformerStep =
    (json: JsObject) => movePath(from, to, json)

  def movePath(from: JsPath, to: JsPath, json: JsObject): Either[JsError, JsObject] = {
    from.asSingleJson(json).toOption match {
      case Some(value) =>
        val withoutOld = prunePath(from)(json)
        setPath(to, value, withoutOld)
      case None        =>
        Right(json)
    }
  }

  def setPath(path: JsPath, value: JsValue, json: JsObject): Either[JsError, JsObject] = {
    path.json.prune andThen
      path.json.put(value)
    Json.obj(path.path.lastOption.map(_.toString).getOrElse("unknown") -> value) // fallback

    setNested(json, path, value)
  }

  /** Recursively removes a value at the given JsPath from the JSON object.
    *
    * ⚠️ This function performs *aggressive pruning*: if removing the value causes its parent object to become empty, the parent is also removed. This is done
    * to remove empty objects that get attached to json paths when moving objects.
    *
    *   - If other transformations depend on the parent key still existing, this may cause failures.
    *   - It introduces asymmetry: reversing a transformation may require reintroducing structure.
    *
    * It does simplify the final JSON and avoid leaving behind empty stubs but keep an eye on this behavior and consider refactoring to allow configurable
    * pruning if necessary.
    */
  def prunePath(path: JsPath)(json: JsObject): JsObject = {
    path.path match {
      case Seq(KeyPathNode(key)) =>
        json - key

      case Seq(KeyPathNode(parent), rest @ _*) =>
        val restPath: JsPath = rest.foldLeft(JsPath()) {
          case (acc: JsPath, key: KeyPathNode) => acc \ key.key
          case (acc, _)                        => acc
        }

        val updatedChild = (json \ parent).asOpt[JsObject].map { child =>
          prunePath(restPath)(child)
        }

        updatedChild match {
          case Some(childUpdated) if childUpdated.fields.isEmpty =>
            json - parent
          case Some(childUpdated)                                =>
            json + (parent -> childUpdated)
          case None                                              =>
            json
        }

      case _ => json
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
