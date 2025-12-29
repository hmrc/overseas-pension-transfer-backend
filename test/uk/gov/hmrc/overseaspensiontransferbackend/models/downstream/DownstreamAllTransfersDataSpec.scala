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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.models.Compiled
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{AllTransfersItem, QtNumber}

import java.time.{Instant, LocalDate}

class DownstreamAllTransfersDataSpec extends AnyFreeSpec with Matchers with SpecBase {

  "DownstreamAllTransfersData JSON formats" - {

    "must read a valid payload with multiple items and set nonEmpty=true" in {
      val json = Json.parse(
        s"""
           |{
           |  "success": {
           |    "qropsTransferOverview": [
           |      {
           |        "fbNumber": "123456000023",
           |        "qtReference": "QT564321",
           |        "qtVersion": "001",
           |        "qtStatus": "Compiled",
           |        "qtDigitalStatus": "Compiled",
           |        "nino": "$testNino",
           |        "firstName": "David",
           |        "lastName": "Warne",
           |        "qtDate": "2025-03-14",
           |        "qropsReference": "QROPS654321",
           |        "submissionCompilationDate": "2025-05-09T10:10:12Z"
           |      },
           |      {
           |        "fbNumber": "123456000024",
           |        "qtReference": "QT564322",
           |        "qtVersion": "003",
           |        "qtStatus": "Submitted",
           |        "submissionCompilationDate": "2025-05-09T19:10:12Z"
           |      }
           |    ]
           |  }
           |}
           |""".stripMargin
      )

      val JsSuccess(model, _) = json.validate[DownstreamAllTransfersData]

      val items = model.success.qropsTransferOverview
      items must have size 2

      val first = items.head
      first.fbNumber                  mustBe "123456000023"
      first.qtReference               mustBe "QT564321"
      first.qtVersion                 mustBe "001"
      first.qtStatus                  mustBe "Compiled"
      first.qtDigitalStatus           mustBe Some("Compiled")
      first.nino                      mustBe Some(testNino)
      first.firstName                 mustBe Some("David")
      first.lastName                  mustBe Some("Warne")
      first.qtDate                    mustBe Some(LocalDate.parse("2025-03-14"))
      first.qropsReference            mustBe Some("QROPS654321")
      first.submissionCompilationDate mustBe Instant.parse("2025-05-09T10:10:12Z")

      val second = items(1)
      second.qtReference               mustBe "QT564322"
      second.qtStatus                  mustBe "Submitted"
      second.qtDigitalStatus           mustBe None
      second.nino                      mustBe None
      second.firstName                 mustBe None
      second.lastName                  mustBe None
      second.qtDate                    mustBe None
      second.qropsReference            mustBe None
      second.submissionCompilationDate mustBe Instant.parse("2025-05-09T19:10:12Z")
    }

    "must round-trip (write then read) and preserve data" in {
      val in = DownstreamAllTransfersData(
        DownstreamAllTransfersData.Payload(
          qropsTransferOverview = List(
            DownstreamAllTransfersData.OverviewItem(
              fbNumber                  = "123456000023",
              qtReference               = "QT564321",
              qtVersion                 = "001",
              qtStatus                  = "Compiled",
              qtDigitalStatus           = Some("Compiled"),
              nino                      = Some(testNino),
              firstName                 = Some("David"),
              lastName                  = Some("Warne"),
              qtDate                    = Some(LocalDate.parse("2025-03-14")),
              qropsReference            = Some("QROPS654321"),
              submissionCompilationDate = Instant.parse("2025-05-09T10:10:12Z")
            )
          )
        )
      )

      val jsonOut              = Json.toJson(in)
      val JsSuccess(backIn, _) = jsonOut.validate[DownstreamAllTransfersData]

      backIn                                                          mustBe in
      (jsonOut \ "success" \ "qropsTransferOverview").as[JsArray].value must have size 1
    }
  }

  "filterForHighestVersion" - {
    val item = DownstreamAllTransfersData.OverviewItem(
      fbNumber                  = "123456000023",
      qtReference               = "QT564321",
      qtVersion                 = "001",
      qtStatus                  = "Submitted",
      qtDigitalStatus           = Some("Submitted"),
      nino                      = Some(testNino),
      firstName                 = Some("David"),
      lastName                  = Some("Warne"),
      qtDate                    = Some(LocalDate.parse("2025-03-14")),
      qropsReference            = Some("QROPS654321"),
      submissionCompilationDate = Instant.parse("2025-05-09T10:10:12Z")
    )

    "Return highest version where multiple records are returned" in {
      val data =
        DownstreamAllTransfersData(
          DownstreamAllTransfersData.Payload(
            qropsTransferOverview = List(
              item.copy(qtVersion = "003"),
              item.copy(qtVersion = "002"),
              item,
              item.copy(qtVersion = "004")
            )
          )
        )

      DownstreamAllTransfersData.filterForHighestVersion(data.success.qropsTransferOverview, Nil) mustBe
        DownstreamAllTransfersData(
          DownstreamAllTransfersData.Payload(
            qropsTransferOverview = List(item.copy(qtVersion = "004"))
          )
        )
    }

    "return only record when one version exists for qtReference" in {
      val data =
        DownstreamAllTransfersData(
          DownstreamAllTransfersData.Payload(
            qropsTransferOverview = List(
              item
            )
          )
        )

      DownstreamAllTransfersData.filterForHighestVersion(data.success.qropsTransferOverview, Nil) mustBe
        DownstreamAllTransfersData(
          DownstreamAllTransfersData.Payload(
            qropsTransferOverview = List(item)
          )
        )
    }

    "return records for all qtReferences present in the original data" in {
      val item2 = item.copy(qtReference = "QT222222")
      val item3 = item.copy(qtReference = "QT333333")
      val item4 = item.copy(qtReference = "QT444444")

      val data =
        DownstreamAllTransfersData(
          DownstreamAllTransfersData.Payload(
            qropsTransferOverview = List(
              item,
              item.copy(qtVersion  = "002"),
              item2.copy(qtVersion = "003"),
              item2,
              item2.copy(qtVersion = "002"),
              item4,
              item3,
              item3.copy(qtVersion = "002"),
              item3.copy(qtVersion = "003"),
              item3.copy(qtVersion = "004"),
              item3.copy(qtVersion = "005"),
              item3.copy(qtVersion = "006"),
              item3.copy(qtVersion = "007"),
              item3.copy(qtVersion = "007"),
              item3.copy(qtVersion = "008"),
              item3.copy(qtVersion = "009"),
              item3.copy(qtVersion = "010")
            )
          )
        )

      DownstreamAllTransfersData.filterForHighestVersion(data.success.qropsTransferOverview, Nil) mustBe
        DownstreamAllTransfersData(
          DownstreamAllTransfersData.Payload(
            qropsTransferOverview = List(
              item.copy(qtVersion  = "002"),
              item2.copy(qtVersion = "003"),
              item4,
              item3.copy(qtVersion = "010")
            )
          )
        )
    }
  }

  "toAllTransfersItem" - {
    "Convert to AllTransfersItem" in {
      val data =
        DownstreamAllTransfersData(
          DownstreamAllTransfersData.Payload(
            qropsTransferOverview = List(
              DownstreamAllTransfersData.OverviewItem(
                fbNumber                  = "123456000023",
                qtReference               = "QT564321",
                qtVersion                 = "001",
                qtStatus                  = "Compiled",
                qtDigitalStatus           = Some("Compiled"),
                nino                      = Some(testNino),
                firstName                 = Some("David"),
                lastName                  = Some("Warne"),
                qtDate                    = Some(LocalDate.parse("2025-03-14")),
                qropsReference            = Some("QROPS654321"),
                submissionCompilationDate = Instant.parse("2025-05-09T10:10:12Z")
              )
            )
          )
        )

      DownstreamAllTransfersData.toAllTransferItems(pstr, data) mustBe
        List(AllTransfersItem(
          transferId      = QtNumber("QT564321"),
          qtVersion       = Some("001"),
          nino            = Some(testNino),
          memberFirstName = Some("David"),
          memberSurname   = Some("Warne"),
          submissionDate  = Some(Instant.parse("2025-05-09T10:10:12Z")),
          lastUpdated     = None, // in-progress supplies lastUpdated
          qtStatus        = Some(Compiled),
          pstrNumber      = Some(pstr),
          qtDate          = Some(LocalDate.parse("2025-03-14"))
        ))
    }
  }
}
