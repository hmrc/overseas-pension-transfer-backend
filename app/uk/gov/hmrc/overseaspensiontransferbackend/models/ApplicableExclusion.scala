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

import play.api.libs.json._

trait ApplicableExclusion {
  val downstreamValue: String
}

object ApplicableExclusion {

  def apply(input: String): ApplicableExclusion =
    input match {
      case Occupational.downstreamValue     => Occupational
      case PublicService.downstreamValue    => PublicService
      case InternationalOrg.downstreamValue => InternationalOrg
      case Resident.downstreamValue         => Resident
    }

  implicit val reads: Reads[ApplicableExclusion] =
    Reads {
      case JsString("occupational") | JsString("01")     => JsSuccess(Occupational)
      case JsString("publicService") | JsString("02")    => JsSuccess(PublicService)
      case JsString("internationalOrg") | JsString("03") => JsSuccess(InternationalOrg)
      case JsString("resident") | JsString("04")         => JsSuccess(Resident)
      case _                                             => JsError("Invalid value provided for SchemeManagerType")
    }

  implicit val writes: Writes[ApplicableExclusion] =
    Writes {
      applicableExclusion => JsString(applicableExclusion.downstreamValue)
    }

  implicit val format: Format[ApplicableExclusion] =
    Format(reads, writes)
}

case object Occupational extends ApplicableExclusion {
  override def toString: String        = "occupational"
  override val downstreamValue: String = "01"
}

case object PublicService extends ApplicableExclusion {
  override def toString: String        = "publicService"
  override val downstreamValue: String = "02"
}

case object InternationalOrg extends ApplicableExclusion {
  override def toString: String        = "internationalOrg"
  override val downstreamValue: String = "03"
}

case object Resident extends ApplicableExclusion {
  override def toString: String        = "resident"
  override val downstreamValue: String = "04"
}
