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
import play.api.libs.functional.syntax.toFunctionalBuilderOps

case class ReceivingQropsEstablishedDetails(
    qropsEstablished: Option[Country],
    qropsEstablishedOther: Option[String]
  )

object ReceivingQropsEstablishedDetails {

  implicit val reads: Reads[ReceivingQropsEstablishedDetails] = (
    (__ \ "qropsEstablished").readNullable[Country] and
      (__ \ "qropsEstablishedOther").readNullable[String]
  )(ReceivingQropsEstablishedDetails.apply _)

  implicit val writes: OWrites[ReceivingQropsEstablishedDetails] =
    Json.writes[ReceivingQropsEstablishedDetails]

  implicit val format: OFormat[ReceivingQropsEstablishedDetails] =
    OFormat(reads, writes)
}
