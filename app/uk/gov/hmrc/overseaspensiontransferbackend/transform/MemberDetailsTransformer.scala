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

object MemberDetailsTransformer {

  def cleanse(json: JsObject): Either[JsError, JsObject] = {
    json.transform(
      (__ \ "memberDetails").json.update(
        (__ \ "memberName").read[JsObject].map { name =>
          val firstName = Json.toJsFieldJsValueWrapper((name \ "firstName").getOrElse(JsNull))
          val lastName  = Json.toJsFieldJsValueWrapper((name \ "lastName").getOrElse(JsNull))

          val base = (json \ "memberDetails").asOpt[JsObject].getOrElse(Json.obj()) - "memberName"

          base ++ Json.obj(
            "firstName" -> firstName,
            "lastName"  -> lastName
          )
        }
      )
    ).asEither.left.map(JsError.apply)
  }

  def enrich(json: JsObject): Either[JsError, JsObject] = {
    val maybeFirst = (json \ "memberDetails" \ "firstName").asOpt[JsValue]
    val maybeLast  = (json \ "memberDetails" \ "lastName").asOpt[JsValue]

    if (maybeFirst.isDefined || maybeLast.isDefined) {
      val memberName = Json.obj(
        "firstName" -> Json.toJsFieldJsValueWrapper(maybeFirst.getOrElse(JsNull)),
        "lastName"  -> Json.toJsFieldJsValueWrapper(maybeLast.getOrElse(JsNull))
      )

      json.transform {
        (__ \ "memberDetails").json.update(
          __.read[JsObject].map { md =>
            md - "firstName" - "lastName" ++ Json.obj("memberName" -> memberName)
          }
        )
      }.asEither.left.map(JsError.apply)
    } else {
      Right(json)
    }
  }
}
