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

import play.api.libs.json.*
import uk.gov.hmrc.overseaspensiontransferbackend.models.{PstrNumber, QtStatus}
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{AllTransfersItem, QtNumber}

import java.time.{Instant, LocalDate}
import scala.annotation.tailrec

final case class DownstreamAllTransfersData(success: DownstreamAllTransfersData.Payload)

object DownstreamAllTransfersData {

  final case class Payload(qropsTransferOverview: List[OverviewItem])

  final case class OverviewItem(
      fbNumber: String,
      qtReference: String,
      qtVersion: String,
      qtStatus: String,
      qtDigitalStatus: Option[String],
      nino: Option[String],
      firstName: Option[String],
      lastName: Option[String],
      qtDate: Option[LocalDate],
      qropsReference: Option[String],
      submissionCompilationDate: Instant
    )

  @tailrec
  def filterForHighestVersion(curr: List[OverviewItem], acc: List[OverviewItem]): DownstreamAllTransfersData =
    curr match {
      case Nil          => DownstreamAllTransfersData(Payload(acc))
      case head :: tail =>
        if (tail.isEmpty) {
          filterForHighestVersion(tail, head :: acc)
        } else {
          val allVersions      = curr.filter(item => head.qtReference == item.qtReference)
          val sorted           = allVersions.sortWith(_.qtVersion.toInt > _.qtVersion.toInt)
          val removeDuplicates = tail.filterNot(item => head.qtReference == item.qtReference)

          filterForHighestVersion(removeDuplicates, acc :+ sorted.head)
        }
    }

  def toAllTransferItems(pstrNumber: PstrNumber, d: DownstreamAllTransfersData): Seq[AllTransfersItem] =
    filterForHighestVersion(d.success.qropsTransferOverview, Nil).success.qropsTransferOverview.map { r =>
      AllTransfersItem(
        transferId      = QtNumber(r.qtReference),
        qtVersion       = Some(r.qtVersion),
        nino            = r.nino,
        memberFirstName = r.firstName,
        memberSurname   = r.lastName,
        submissionDate  = Some(r.submissionCompilationDate),
        lastUpdated     = None, // in-progress supplies lastUpdated
        qtStatus        = Some(QtStatus(r.qtStatus)),
        pstrNumber      = Some(pstrNumber),
        qtDate          = r.qtDate
      )
    }

  implicit val overviewItemFormat: OFormat[OverviewItem]       = Json.format[OverviewItem]
  implicit val payloadFormat: OFormat[Payload]                 = Json.format[Payload]
  implicit val dataFormat: OFormat[DownstreamAllTransfersData] = Json.format[DownstreamAllTransfersData]
}
