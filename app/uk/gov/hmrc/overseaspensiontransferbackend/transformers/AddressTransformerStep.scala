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

import play.api.libs.json.{JsError, JsObject, JsPath, JsString, Json}
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

trait AddressTransformerStep extends JsonHelpers {

  // Converts a flat frontend-style address at the given path into a nested backend-style address (addressDetails + optional poBox).
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

        setPath(path, finalObj, json)

      case None => Right(json)
    }
  }

  // Converts a nested backend-style address at the given path back into a flat frontend-style address.
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

        setPath(path, rebuilt, json)

      case None => Right(json)
    }
  }

}
