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

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

trait NameTransformers extends JsonHelpers {

  /** Flattens a nested JSON representation of a person's name into a flat structure.
    *
    * This is designed to work with data structures like: { "name": { "firstName": "Jane", "lastName": "Doe" } } which will be flattened to: { "foreName":
    * "Jane", "lastName": "Doe" }
    */
  def flattenName(
      path: JsPath,
      foreNameKey: String = "foreName",
      lastNameKey: String = "lastName"
    )(
      json: JsObject
    ): Either[JsError, JsObject] = {
    path.asSingleJson(json).asOpt[JsObject] match {
      case Some(nameObj) =>
        val firstNameOpt = (nameObj \ "firstName").asOpt[String]
        val lastNameOpt  = (nameObj \ "lastName").asOpt[String]

        val nameFields = Seq(
          firstNameOpt.map(foreNameKey -> JsString(_)),
          lastNameOpt.map(lastNameKey -> JsString(_))
        ).flatten

        val nameObject = JsObject(nameFields)

        val parentPath = JsPath(path.path.dropRight(1))

        setPath(parentPath, nameObject, json)

      case None =>
        Right(json)
    }
  }

  /** Unflattens a nested JSON representation of a person's name into a flat structure.
    */
  def unflattenName(
      foreNameKey: String = "foreName",
      lastNameKey: String = "lastName",
      path: JsPath
    ): JsObject => Either[JsError, JsObject] = { json =>
    val parentPath = JsPath(path.path.dropRight(1))
    val parentOpt  = parentPath.asSingleJson(json).asOpt[JsObject]

    parentOpt match {
      case Some(parent) =>
        val foreNameOpt = (parent \ foreNameKey).asOpt[String]
        val lastNameOpt = (parent \ lastNameKey).asOpt[String]

        if (foreNameOpt.isEmpty && lastNameOpt.isEmpty) {
          Right(json)
        } else {
          val nameFields    = Seq(
            foreNameOpt.map("firstName" -> JsString(_)),
            lastNameOpt.map("lastName" -> JsString(_))
          ).flatten
          val nameObject    = JsObject(nameFields)
          val updatedParent = parent - foreNameKey - lastNameKey
          val withName      = updatedParent + (path.path.last.asInstanceOf[KeyPathNode].key -> nameObject)

          setPath(parentPath, withName, json)
        }

      case None =>
        Right(json)
    }
  }
}
