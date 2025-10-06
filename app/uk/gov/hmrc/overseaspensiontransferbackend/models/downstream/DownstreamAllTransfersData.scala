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
import uk.gov.hmrc.overseaspensiontransferbackend.models.{PstrNumber, QtStatus}
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{AllTransfersItem, QtNumber}

import java.time.{Instant, LocalDate}

final case class DownstreamAllTransfersData(success: DownstreamAllTransfersData.Payload)

object DownstreamAllTransfersData {

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

  def toAllTransferItems(pstrNumber: PstrNumber, d: DownstreamAllTransfersData): Seq[AllTransfersItem] =
    d.success.qropsTransferOverview.map { r =>
      AllTransfersItem(
        transferReference = None,
        qtReference       = Some(QtNumber(r.qtReference)),
        qtVersion         = Some(r.qtVersion),
        nino              = Some(r.nino),
        memberFirstName   = Some(r.firstName),
        memberSurname     = Some(r.lastName),
        submissionDate    = Some(r.submissionCompilationDate),
        lastUpdated       = None, // in-progress supplies lastUpdated
        qtStatus          = Some(QtStatus(r.qtStatus)),
        pstrNumber        = Some(pstrNumber),
        qtDate            = Some(r.qtDate)
      )
    }

  implicit val overviewItemFormat: OFormat[OverviewItem]       = Json.format[OverviewItem]
  implicit val payloadFormat: OFormat[Payload]                 = Json.format[Payload]
  implicit val dataFormat: OFormat[DownstreamAllTransfersData] = Json.format[DownstreamAllTransfersData]
}
