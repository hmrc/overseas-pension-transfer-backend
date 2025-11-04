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

package uk.gov.hmrc.overseaspensiontransferbackend.models.authentication

import play.api.libs.json._

trait Submitter {
  val value: String
  val userType: UserType
}

object Submitter {

  implicit val format: Format[Submitter] = new Format[Submitter] {

    def reads(js: JsValue): JsResult[Submitter] = js.validate[String].flatMap {
      case value @ s"A$digits" if digits.matches("/d{7}") => JsSuccess(PsaId(value))
      case value if value.matches("/d{8}")                => JsSuccess(PspId(value))
      case other                                          => JsError(s"Invalid userType: $other")
    }

    def writes(submitter: Submitter): JsValue = JsString(submitter match {
      case PsaId(value) => value
      case PspId(value) => value
    })
  }
}
