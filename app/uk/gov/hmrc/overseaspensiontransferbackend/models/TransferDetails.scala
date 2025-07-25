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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

import java.time.LocalDate

case class TransferDetails(
    transferAmount: Option[BigDecimal],
    allowanceBeforeTransfer: Option[BigDecimal],
    dateMemberTransferred: Option[LocalDate],
    cashOnlyTransfer: Option[String],
    paymentTaxableOverseas: Option[String],
    taxableOverseasTransferDetails: Option[TaxableOverseasTransferDetails]
  )

object TransferDetails {

  implicit val reads: Reads[TransferDetails] = (
    (__ \ "transferAmount").readNullable[BigDecimal] and
      (__ \ "allowanceBeforeTransfer").readNullable[BigDecimal] and
      (__ \ "dateMemberTransferred").readNullable[LocalDate] and
      (__ \ "cashOnlyTransfer").readNullable[String] and
      (__ \ "paymentTaxableOverseas").readNullable[String] and
      (__ \ "taxableOverseasTransferDetails").readNullable[TaxableOverseasTransferDetails]
  )(TransferDetails.apply _)

  implicit val writes: OWrites[TransferDetails] =
    Json.writes[TransferDetails]

  implicit val format: OFormat[TransferDetails] =
    OFormat(reads, writes)
}
