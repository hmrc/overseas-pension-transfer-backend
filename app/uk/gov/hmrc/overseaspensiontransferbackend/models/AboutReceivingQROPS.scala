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

case class AboutReceivingQROPS(
    qropsFullName: Option[String],
    qropsRef: Option[String],
    receivingQropsAddress: Option[ReceivingQropsAddress],
    receivingQropsEstablishedDetails: Option[ReceivingQropsEstablishedDetails],
    qropsSchemeManagerType: Option[QROPSSchemeManagerType]
  )

object AboutReceivingQROPS {

  implicit val reads: Reads[AboutReceivingQROPS] = (
    (__ \ "qropsFullName").readNullable[String] and
      (__ \ "qropsRef").readNullable[String] and
      (__ \ "receivingQropsAddress").readNullable[ReceivingQropsAddress] and
      (__ \ "receivingQropsEstablishedDetails").readNullable[ReceivingQropsEstablishedDetails] and
      (__ \ "qropsSchemeManagerType").readNullable[QROPSSchemeManagerType]
  )(AboutReceivingQROPS.apply _)

  implicit val writes: OWrites[AboutReceivingQROPS] =
    Json.writes[AboutReceivingQROPS]

  implicit val format: OFormat[AboutReceivingQROPS] =
    OFormat(reads, writes)
}
