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

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

trait AddressTransformerStep extends JsonHelpers {

  /* This constructs an address at a path inside a nestedKey so for example if you pass addressDetails as a nested key
   * and memberDetails \ principalResAdd as a path, it will create the address at memberDetails \ principleResAdd \ addressDetails */
  def constructAddressAt(path: JsPath, nestedKey: String): JsObject => Either[JsError, JsObject] = { json =>
    path.asSingleJson(json).asOpt[JsObject] match {
      case Some(addressObj) =>
        val addressFields   = extractAddressFields(addressObj)
        val preservedFields = addressObj.fields.filterNot {
          case (key, _) => addressFields.map(_._1).contains(key)
        }

        val rebuilt = Json.obj(nestedKey -> JsObject(addressFields)) ++ JsObject(preservedFields)

        setPath(path, rebuilt, json)

      case None => Right(json)
    }
  }

  /* This deconstructs an address in place, so if it is constructed at memberDetails \ memberAddress (where memberAddress is the nested key)
   * it will need to be deconstructed there and then moved from memberDetails \ memberAddress (including the nested key) */
  def deconstructAddressAt(path: JsPath, nestedKey: String): JsObject => Either[JsError, JsObject] = { json =>
    path.asSingleJson(json).asOpt[JsObject] match {
      case Some(container) =>
        val nestedObj = (container \ nestedKey).asOpt[JsObject].getOrElse(Json.obj())
        val flattened = JsObject(extractAddressFields(nestedObj))

        val preserved = container.fields.filterNot(_._1 == nestedKey)
        val rebuilt   = JsObject(preserved :+ (nestedKey -> flattened))

        setPath(path, rebuilt, json)

      case None => Right(json)
    }
  }

  private def extractAddressFields(jsObj: JsObject): Seq[(String, JsValue)] =
    Seq(
      "addressLine1" -> (jsObj \ "addressLine1").asOpt[String].map(JsString),
      "addressLine2" -> (jsObj \ "addressLine2").asOpt[String].map(JsString),
      "addressLine3" -> (jsObj \ "addressLine3").asOpt[String].map(JsString),
      "addressLine4" -> (jsObj \ "addressLine4").asOpt[String].map(JsString),
      "addressLine5" -> (jsObj \ "addressLine5").asOpt[String].map(JsString),
      "ukPostCode"   -> (jsObj \ "ukPostCode").asOpt[String].map(JsString),
      "country"      -> (jsObj \ "country").asOpt[JsObject]
    ).collect {
      case (key, Some(value)) => key -> value
    }
}
