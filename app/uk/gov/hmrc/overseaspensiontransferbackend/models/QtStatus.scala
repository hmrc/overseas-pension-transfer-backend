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

trait QtStatus {
  val downstreamValue: String
}

object QtStatus {

  def apply(input: String): QtStatus =
    input match {
      case Compiled.downstreamValue        => Compiled
      case Submitted.downstreamValue       => Submitted
      case InProgress.downstreamValue      => InProgress
      case AmendInProgress.downstreamValue => AmendInProgress
    }

  implicit val reads: Reads[QtStatus] =
    Reads {
      case JsString("Compiled")        => JsSuccess(Compiled)
      case JsString("Submitted")       => JsSuccess(Submitted)
      case JsString("InProgress")      => JsSuccess(InProgress)
      case JsString("AmendInProgress") => JsSuccess(AmendInProgress)
      case _                           => JsError("Invalid value provided for QtStatus")
    }

  implicit val writes: Writes[QtStatus] =
    Writes {
      qtStatus => JsString(qtStatus.downstreamValue)
    }

  implicit val format: Format[QtStatus] =
    Format(reads, writes)
}

case object Compiled extends QtStatus {
  override def toString: String        = "Compiled"
  override val downstreamValue: String = toString
}

case object Submitted extends QtStatus {
  override def toString: String        = "Submitted"
  override val downstreamValue: String = toString
}

case object InProgress extends QtStatus {
  override def toString: String        = "InProgress"
  override val downstreamValue: String = toString
}

case object AmendInProgress extends QtStatus {
  override def toString: String        = "AmendInProgress"
  override val downstreamValue: String = toString
}
