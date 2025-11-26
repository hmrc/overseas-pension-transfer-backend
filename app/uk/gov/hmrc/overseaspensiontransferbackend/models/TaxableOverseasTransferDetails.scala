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
import play.api.libs.json.{__, Format, Json, Reads, Writes}
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

case class TaxableOverseasTransferDetails(
    whyTaxableOT: Option[WhyTransferTaxable],
    applicableExclusion: Option[Seq[ApplicableExclusion]],
    amountTaxDeducted: Option[BigDecimal],
    transferMinusTax: Option[BigDecimal]
  )

object TaxableOverseasTransferDetails extends JsonHelpers {

  implicit val reads: Reads[TaxableOverseasTransferDetails] = (
    (__ \ "whyTaxableOT").readNullable[WhyTransferTaxable] and
      (__ \ "applicableExclusion").readNullable[Seq[ApplicableExclusion]] and
      (__ \ "amountTaxDeducted").readNullable[BigDecimal] and
      (__ \ "transferMinusTax").readNullable[BigDecimal]
  )(TaxableOverseasTransferDetails.apply _)

  implicit val writes: Writes[TaxableOverseasTransferDetails] = Json.writes[TaxableOverseasTransferDetails]

  val auditWrites: Writes[TaxableOverseasTransferDetails] = { taxableOverseasTransferDetails =>
    optField("reasonCode", taxableOverseasTransferDetails.whyTaxableOT) ++
      optField("applicableExclusionsCodes", taxableOverseasTransferDetails.applicableExclusion) ++
      optField("amountOfTaxDeducted", taxableOverseasTransferDetails.amountTaxDeducted) ++
      optField("transferAmountMinusTaxAmount", taxableOverseasTransferDetails.transferMinusTax)
  }

  implicit val format: Format[TaxableOverseasTransferDetails] = Format(reads, writes)
}
