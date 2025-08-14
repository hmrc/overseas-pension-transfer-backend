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

package uk.gov.hmrc.overseaspensiontransferbackend.models.submission

import play.api.libs.json._

sealed trait UserType
case object Psa extends UserType
case object Psp extends UserType

object UserType {

  implicit val format: Format[UserType] = new Format[UserType] {

    def reads(js: JsValue): JsResult[UserType] = js.validate[String].flatMap {
      case "Psa" => JsSuccess(Psa)
      case "Psp" => JsSuccess(Psp)
      case other => JsError(s"Invalid userType: $other")
    }

    def writes(ut: UserType): JsValue = JsString(ut match {
      case Psa => "Psa"
      case Psp => "Psp"
    })
  }
}
