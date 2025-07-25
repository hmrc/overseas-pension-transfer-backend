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

package uk.gov.hmrc.overseaspensiontransferbackend.models

import play.api.libs.json.{Format, JsError, JsString, JsSuccess, Reads, Writes}

trait SchemeManagerType {
  val downstreamValue: String
}

object SchemeManagerType {

  def apply(value: String): SchemeManagerType =
    value match {
      case Individual.downstreamValue   => Individual
      case Organisation.downstreamValue => Organisation
    }

  implicit val reads: Reads[SchemeManagerType] =
    Reads {
      case JsString("individual") | JsString("01")   => JsSuccess(Individual)
      case JsString("organisation") | JsString("02") => JsSuccess(Organisation)
      case _                                         => JsError("Invalid value provided for SchemeManagerType")
    }

  implicit val writes: Writes[SchemeManagerType] =
    Writes {
      schemeManagerType => JsString(schemeManagerType.downstreamValue)
    }

  implicit val format: Format[SchemeManagerType] =
    Format(reads, writes)
}

case object Individual extends SchemeManagerType {
  override def toString: String = "individual"

  override val downstreamValue: String = "01"
}

case object Organisation extends SchemeManagerType {
  override def toString: String = "organisation"

  override val downstreamValue: String = "02"
}
