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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.overseaspensiontransferbackend.base.TestAppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{AllTransfersItem, QtNumber, TransferNumber}
import uk.gov.hmrc.overseaspensiontransferbackend.services.EncryptionService

import java.time.Instant

class SavedUserAnswersSpec extends AnyFreeSpec with Matchers {

  implicit private val encryptionService: EncryptionService = TestAppConfig.encryptionService

  "SavedUserAnswers JSON format" - {

    "must round trip to and from JSON" in {
      val original = SavedUserAnswers(
        transferId  = TransferNumber("ref-123"),
        pstr        = PstrNumber("12345678AB"),
        data        = AnswersData(
          reportDetails       = None,
          transferringMember  = Some(TransferringMember(None)),
          aboutReceivingQROPS = None,
          transferDetails     = None
        ),
        lastUpdated = Instant.parse("2024-01-01T12:00:00Z")
      )

      val json   = Json.toJson(original)
      val parsed = json.validate[SavedUserAnswers]

      parsed mustBe JsSuccess(original)
    }

    "must flatten AnswersData to JsObject in writes" in {
      val data = AnswersData(
        reportDetails       = None,
        transferringMember  = Some(TransferringMember(Some(MemberDetails(foreName = Some("Jane"))))),
        aboutReceivingQROPS = None,
        transferDetails     = None
      )

      val obj = SavedUserAnswers(TransferNumber("ref-456"), PstrNumber("12345678AB"), data, Instant.parse("2025-01-01T10:00:00Z"))

      val json = Json.toJson(obj)

      (json \ "data" \ "transferringMember" \ "memberDetails" \ "foreName").as[String] mustBe "Jane"
      (json \ "_id").as[String]                                                        mustBe "ref-456"
    }
  }

  "AnswersDataWrapper format" - {

    "must serialise and deserialize DecryptedAnswersData" in {
      val decrypted: AnswersDataWrapper = DecryptedAnswersData(
        AnswersData(None, Some(TransferringMember(None)), None, None)
      )

      val json   = Json.toJson(decrypted)(AnswersDataWrapper.wrapperFormat)
      val parsed = json.validate[AnswersDataWrapper](AnswersDataWrapper.wrapperFormat)

      parsed mustBe JsSuccess(decrypted)
    }

    "must serialise and deserialize EncryptedAnswersData" in {
      val decrypted                     = DecryptedAnswersData(
        AnswersData(None, Some(TransferringMember(None)), None, None)
      )
      val encrypted: AnswersDataWrapper = decrypted.encrypt

      val json   = Json.toJson(encrypted)(AnswersDataWrapper.wrapperFormat)
      val parsed = json.validate[AnswersDataWrapper](AnswersDataWrapper.wrapperFormat)

      parsed mustBe JsSuccess(encrypted)
    }
  }

  "toAllTransfersItem" - {
    "return an InProgress Item when TransferId is TransferNumber" in {
      val savedAnswers = SavedUserAnswers(
        transferId  = TransferNumber("ref-123"),
        pstr        = PstrNumber("12345678AB"),
        data        = AnswersData(
          reportDetails       = None,
          transferringMember  = Some(TransferringMember(Some(MemberDetails(Some("Forename"), Some("Lastname"), None, Some("AA000000A"))))),
          aboutReceivingQROPS = None,
          transferDetails     = None
        ),
        lastUpdated = Instant.parse("2024-01-01T12:00:00Z")
      )

      savedAnswers.toAllTransfersItem mustBe
        AllTransfersItem(
          transferId      = TransferNumber("ref-123"),
          qtVersion       = None,
          qtStatus        = Some(InProgress),
          nino            = Some("AA000000A"),
          memberFirstName = Some("Forename"),
          memberSurname   = Some("Lastname"),
          qtDate          = None,
          lastUpdated     = Some(Instant.parse("2024-01-01T12:00:00Z")),
          pstrNumber      = Some(PstrNumber("12345678AB")),
          submissionDate  = None
        )
    }

    "return an AmendInProgress item when the TransferId is a QtNumber" in {
      val savedAnswers = SavedUserAnswers(
        transferId  = QtNumber("QT123456"),
        pstr        = PstrNumber("12345678AB"),
        data        = AnswersData(
          reportDetails       = None,
          transferringMember  = Some(TransferringMember(Some(MemberDetails(Some("Forename"), Some("Lastname"), None, Some("AA000000A"))))),
          aboutReceivingQROPS = None,
          transferDetails     = None
        ),
        lastUpdated = Instant.parse("2024-01-01T12:00:00Z")
      )

      savedAnswers.toAllTransfersItem mustBe
        AllTransfersItem(
          transferId      = QtNumber("QT123456"),
          qtVersion       = None,
          qtStatus        = Some(AmendInProgress),
          nino            = Some("AA000000A"),
          memberFirstName = Some("Forename"),
          memberSurname   = Some("Lastname"),
          qtDate          = None,
          lastUpdated     = Some(Instant.parse("2024-01-01T12:00:00Z")),
          pstrNumber      = Some(PstrNumber("12345678AB")),
          submissionDate  = None
        )
    }
  }
}
