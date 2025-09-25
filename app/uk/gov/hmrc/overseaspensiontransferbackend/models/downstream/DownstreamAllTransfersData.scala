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

package uk.gov.hmrc.overseaspensiontransferbackend.models.downstream

import play.api.libs.json._

import java.time.{Instant, LocalDate}

final case class DownstreamAllTransfersData(success: DownstreamAllTransfersData.Payload) {
  def nonEmpty: Boolean = success.qropsTransferOverview.nonEmpty
}

object DownstreamAllTransfersData {

  final case class Payload(qropsTransferOverview: List[OverviewItem] = Nil)

  final case class OverviewItem(
      fbNumber: String,
      qtReference: String,
      qtVersion: String,
      qtStatus: String,
      qtDigitalStatus: String,
      nino: String,
      firstName: String,
      lastName: String,
      qtDate: LocalDate,
      qropsReference: String,
      submissionCompilationDate: Instant
    )

  implicit val overviewItemFormat: OFormat[OverviewItem] = Json.format[OverviewItem]

  // account for possible missing payload - return an empty list
  implicit val payloadReads: Reads[Payload]    =
    (__ \ "qropsTransferOverview").readWithDefault[List[OverviewItem]](Nil).map(Payload.apply)
  implicit val payloadWrites: OWrites[Payload] = Json.writes[Payload]

  // account for possible missing success key - return an empty list
  implicit val dataReads: Reads[DownstreamAllTransfersData]    =
    (__ \ "success").readWithDefault[Payload](Payload()).map(DownstreamAllTransfersData.apply)
  implicit val dataWrites: OWrites[DownstreamAllTransfersData] = Json.writes[DownstreamAllTransfersData]
}
