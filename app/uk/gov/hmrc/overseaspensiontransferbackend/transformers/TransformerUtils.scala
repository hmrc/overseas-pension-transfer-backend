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

object TransformerUtils {

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

        JsonHelpers.setPath(parentPath, nameObject, json)

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

          JsonHelpers.setPath(parentPath, withName, json)
        }

      case None =>
        Right(json)
    }
  }

  def constructAddressAt(path: JsPath): JsObject => Either[JsError, JsObject] = { json =>
    path.asSingleJson(json).asOpt[JsObject] match {
      case Some(addressObj) =>
        val addressFields = Seq(
          "addressLine1" -> (addressObj \ "addressLine1").asOpt[String],
          "addressLine2" -> (addressObj \ "addressLine2").asOpt[String],
          "addressLine3" -> (addressObj \ "addressLine3").asOpt[String],
          "addressLine4" -> (addressObj \ "addressLine4").asOpt[String],
          "addressLine5" -> (addressObj \ "addressLine5").asOpt[String],
          "ukPostCode"   -> (addressObj \ "postcode").asOpt[String],
          "country"      -> (addressObj \ "country").asOpt[JsObject]
        ).collect {
          case (k, Some(v: String))   => k -> JsString(v)
          case (k, Some(v: JsObject)) => k -> v
        }

        val addressDetails = JsObject(addressFields)

        val poBox = (addressObj \ "poBox").asOpt[String]

        val finalObj = Json.obj("addressDetails" -> addressDetails) ++
          poBox.map(pb => Json.obj("poBox" -> JsString(pb))).getOrElse(Json.obj())

        JsonHelpers.setPath(path, finalObj, json)

      case None => Right(json)
    }
  }

  def deconstructAddressAt(path: JsPath): JsObject => Either[JsError, JsObject] = { json =>
    path.asSingleJson(json).asOpt[JsObject] match {
      case Some(addressObj) =>
        val details = (addressObj \ "addressDetails").asOpt[JsObject].getOrElse(Json.obj())

        val flattenedFields = Seq(
          "addressLine1" -> (details \ "addressLine1").asOpt[String],
          "addressLine2" -> (details \ "addressLine2").asOpt[String],
          "addressLine3" -> (details \ "addressLine3").asOpt[String],
          "addressLine4" -> (details \ "addressLine4").asOpt[String],
          "addressLine5" -> (details \ "addressLine5").asOpt[String],
          "postcode"     -> (details \ "ukPostCode").asOpt[String],
          "country"      -> (details \ "country").asOpt[JsObject],
          "poBox"        -> (addressObj \ "poBox").asOpt[String]
        ).collect {
          case (key, Some(value: String))   => key -> JsString(value)
          case (key, Some(value: JsObject)) => key -> value
        }

        val rebuilt = JsObject(flattenedFields)

        JsonHelpers.setPath(path, rebuilt, json)

      case None => Right(json)
    }
  }



  def applyPipeline[T](
      json: JsObject,
      steps: Seq[T]
    )(
      stepFn: T => JsObject => Either[JsError, JsObject]
    ): Either[JsError, JsObject] = {
    steps.foldLeft(Right(json): Either[JsError, JsObject]) {
      case (Right(current), transformer) => stepFn(transformer)(current)
      case (err @ Left(_), _)            => err
    }
  }

}
