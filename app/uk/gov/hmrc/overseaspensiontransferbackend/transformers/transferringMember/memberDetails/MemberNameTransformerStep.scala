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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferringMember.memberDetails

import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.TransformerStep

class MemberNameTransformerStep extends TransformerStep {

  override def cleanse(json: JsObject): Either[JsError, JsObject] = {
    (json \ "memberDetails").asOpt[JsObject] match {
      case Some(memberDetails) =>
        (memberDetails \ "memberName").asOpt[JsObject] match {
          case Some(nameObj) =>
            val updates = Seq(
              (nameObj \ "firstName").asOpt[String].map("foreName" -> JsString(_)),
              (nameObj \ "lastName").asOpt[String].map("lastName" -> JsString(_))
            ).flatten

            val updatedMemberDetails =
              memberDetails - "memberName" ++ JsObject(updates)

            val updatedJson = json + ("memberDetails" -> updatedMemberDetails)
            Right(updatedJson)

          case None => Right(json)
        }

      case None => Right(json)
    }
  }

  override def enrich(json: JsObject): Either[JsError, JsObject] = {
    val maybeFore = (json \ "foreName").asOpt[JsValue]
    val maybeLast = (json \ "lastName").asOpt[JsValue]

    if (maybeFore.isDefined || maybeLast.isDefined) {
      val name = Json.obj(
        "firstName" -> Json.toJsFieldJsValueWrapper(maybeFore.getOrElse(JsNull)),
        "lastName"  -> Json.toJsFieldJsValueWrapper(maybeLast.getOrElse(JsNull))
      )

      val updated = json - "foreName" - "lastName" + ("memberName" -> name)
      Right(updated)
    } else {
      Right(json)
    }
  }
}
