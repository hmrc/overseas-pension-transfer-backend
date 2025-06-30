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

package uk.gov.hmrc.overseaspensiontransferbackend.transform

import play.api.libs.json._
import play.api.libs.json.Reads._

object MemberDetailsTransformer extends JsonTransformerStep {

  override def cleanse(json: JsObject): Either[JsError, JsObject] = {
    val maybeMemberName = (json \ "memberDetails" \ "memberName").toOption

    maybeMemberName match {
      case Some(obj: JsObject) =>
        val maybeFirst = (obj \ "firstName").toOption
        val maybeLast  = (obj \ "lastName").toOption

        val updatedFields = maybeFirst.map("firstName" -> _) ++ maybeLast.map("lastName" -> _)

        val newMemberDetails = (json \ "memberDetails")
          .asOpt[JsObject]
          .getOrElse(Json.obj())
          .fields
          .filterNot(_._1 == "memberName") ++ updatedFields

        val updated = json + ("memberDetails" -> JsObject(newMemberDetails))
        Right(updated)

      case Some(_) =>
        Left(JsError(__ \ "memberDetails" \ "memberName", "Expected memberName to be an object"))

      case None =>
        Right(json)
    }
  }

  override def enrich(json: JsObject): Either[JsError, JsObject] = {
    (json \ "memberDetails").asOpt[JsObject] match {
      case Some(memberDetails) =>
        val maybeFirst = (memberDetails \ "firstName").asOpt[JsValue]
        val maybeLast  = (memberDetails \ "lastName").asOpt[JsValue]

        if (maybeFirst.isDefined || maybeLast.isDefined) {
          val memberName = Json.obj(
            "firstName" -> Json.toJsFieldJsValueWrapper(maybeFirst.getOrElse(JsNull)),
            "lastName"  -> Json.toJsFieldJsValueWrapper(maybeLast.getOrElse(JsNull))
          )

          val updated = memberDetails
            .-("firstName")
            .-("lastName")
            .+("memberName" -> memberName)

          Right(json + ("memberDetails" -> updated))
        } else {
          Right(json)
        }

      case None => Right(json)
    }
  }
}
