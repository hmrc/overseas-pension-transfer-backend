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
import java.time.{Instant, LocalDate}

class DownstreamAllTransfersDataSpec extends AnyFreeSpec with Matchers {

  "DownstreamAllTransfersData JSON formats" - {

    "must read a valid payload with multiple items and set nonEmpty=true" in {
      val json = Json.parse(
        """
          |{
          |  "success": {
          |    "qropsTransferOverview": [
          |      {
          |        "fbNumber": "123456000023",
          |        "qtReference": "QT564321",
          |        "qtVersion": "001",
          |        "qtStatus": "Compiled",
          |        "qtDigitalStatus": "Complied",
          |        "nino": "AA000000A",
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
          |        "qtDigitalStatus": "Submitted",
          |        "nino": "AA000001A",
          |        "firstName": "Edith",
          |        "lastName": "Ennis-Hill",
          |        "qtDate": "2025-01-01",
          |        "qropsReference": "QROPS654322",
          |        "submissionCompilationDate": "2025-05-09T19:10:12Z"
          |      }
          |    ]
          |  }
          |}
          |""".stripMargin
      )

      val JsSuccess(model, _) = json.validate[DownstreamAllTransfersData]
      model.nonEmpty mustBe true

      val items = model.success.qropsTransferOverview
      items must have size 2

      val first = items.head
      first.fbNumber                  mustBe "123456000023"
      first.qtReference               mustBe "QT564321"
      first.qtVersion                 mustBe "001"
      first.qtStatus                  mustBe "Compiled"
      first.qtDigitalStatus           mustBe "Complied"
      first.nino                      mustBe "AA000000A"
      first.firstName                 mustBe "David"
      first.lastName                  mustBe "Warne"
      first.qtDate                    mustBe LocalDate.parse("2025-03-14")
      first.qropsReference            mustBe "QROPS654321"
      first.submissionCompilationDate mustBe Instant.parse("2025-05-09T10:10:12Z")

      val second = items(1)
      second.qtReference               mustBe "QT564322"
      second.qtStatus                  mustBe "Submitted"
      second.submissionCompilationDate mustBe Instant.parse("2025-05-09T19:10:12Z")
    }

    "must read 200 OK with missing 'success' as empty list (nonEmpty=false)" in {
      val json                = Json.parse("""{}""")
      val JsSuccess(model, _) = json.validate[DownstreamAllTransfersData]
      model.nonEmpty                      mustBe false
      model.success.qropsTransferOverview mustBe Nil
    }

    "must read 200 OK with empty 'success' object as empty list (nonEmpty=false)" in {
      val json                = Json.parse("""{ "success": {} }""")
      val JsSuccess(model, _) = json.validate[DownstreamAllTransfersData]
      model.nonEmpty                      mustBe false
      model.success.qropsTransferOverview mustBe Nil
    }

    "must read 200 OK with explicit empty array as empty list (nonEmpty=false)" in {
      val json                = Json.parse("""{ "success": { "qropsTransferOverview": [] } }""")
      val JsSuccess(model, _) = json.validate[DownstreamAllTransfersData]
      model.nonEmpty                      mustBe false
      model.success.qropsTransferOverview mustBe Nil
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
              qtDigitalStatus           = "Complied",
              nino                      = "AA000000A",
              firstName                 = "David",
              lastName                  = "Warne",
              qtDate                    = LocalDate.parse("2025-03-14"),
              qropsReference            = "QROPS654321",
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

    "must write empty model with an explicit empty array under success" in {
      val emptyData = DownstreamAllTransfersData(DownstreamAllTransfersData.Payload(Nil))
      val json      = Json.toJson(emptyData)

      (json \ "success").toOption                                      must not be empty
      (json \ "success" \ "qropsTransferOverview").as[JsArray].value mustBe empty
    }
  }
}
