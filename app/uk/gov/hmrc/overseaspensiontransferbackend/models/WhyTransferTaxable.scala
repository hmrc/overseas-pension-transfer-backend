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

import play.api.libs.json.{JsError, JsString, JsSuccess, Reads, Writes}

trait WhyTransferTaxable {
  val downstreamValue: String
}

object WhyTransferTaxable {

  implicit val reads: Reads[WhyTransferTaxable] = Reads {
    case JsString("transferExceedsOTCAllowance") | JsString("01") => JsSuccess(TransferExceedsOTCAllowance)
    case JsString("noExclusion") | JsString("02")                 => JsSuccess(NoExclusion)
    case _                                                        => JsError("Unable to read invalid value for 'WhyTransferTaxable'")
  }

  implicit val writes: Writes[WhyTransferTaxable] = Writes {
    whyTransferTaxable => JsString(whyTransferTaxable.downstreamValue)
  }
}

case object TransferExceedsOTCAllowance extends WhyTransferTaxable {
  override def toString: String = "transferExceedsOTCAllowance"

  override val downstreamValue: String = "01"
}

case object NoExclusion extends WhyTransferTaxable {
  override def toString: String = "noExclusion"

  override val downstreamValue: String = "02"
}
