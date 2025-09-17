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

final case class DownstreamGetAllSuccess(success: DownstreamGetAllSuccess.Payload)

object DownstreamGetAllSuccess {

  final case class Payload(qropsTransferOverview: List[OverviewItem])

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
  implicit val payloadFormat: OFormat[Payload]           = Json.format[Payload]
  implicit val format: OFormat[DownstreamGetAllSuccess]  = Json.format[DownstreamGetAllSuccess]
}
