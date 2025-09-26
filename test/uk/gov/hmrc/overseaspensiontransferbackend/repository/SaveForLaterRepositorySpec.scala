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

package uk.gov.hmrc.overseaspensiontransferbackend.repository

import org.apache.pekko.Done
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.mongo.test.CleanMongoCollectionSupport
import uk.gov.hmrc.overseaspensiontransferbackend.base.TestAppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models._
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.services.EncryptionService

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class SaveForLaterRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with CleanMongoCollectionSupport
    with ScalaFutures
    with MockitoSugar {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = 5.seconds, interval = 50.millis)

  override val databaseName: String = "test-saveforlater"
  private val collectionName        = "saved-user-answers"

  private val now          = Instant.parse("2025-01-01T00:00:00Z")
  private val mutableClock = Clock.fixed(now, ZoneOffset.UTC)
  private val encryption   = TestAppConfig.encryptionService
  private val appConfig    = TestAppConfig.appConfig()

  private val repository = new SaveForLaterRepository(
    mongoComponent    = mongoComponent,
    encryptionService = encryption,
    appConfig         = appConfig,
    clock             = mutableClock
  )

  private val rawCollection =
    mongoComponent.database.getCollection[Document](collectionName)

  // --- Helper to build test data ---
  private def buildUserAnswer(referenceId: String = "ref-useranswer-001"): SavedUserAnswers = {
    val userAnswers = AnswersData(
      reportDetails       = None,
      transferringMember  = None,
      aboutReceivingQROPS = None,
      transferDetails     = Some(
        TransferDetails(
          transferAmount                 = Some(1000),
          allowanceBeforeTransfer        = Some(5000),
          dateMemberTransferred          = Some(java.time.LocalDate.parse("2025-01-01")),
          cashOnlyTransfer               = Some("No"),
          paymentTaxableOverseas         = Some("Yes"),
          reasonNoOverseasTransfer       = None,
          taxableOverseasTransferDetails = None,
          typeOfAssets                   = Some(
            TypeOfAssets(
              cashAssets          = Some("Yes"),
              cashValue           = Some(1000),
              unquotedShareAssets = Some("Yes"),
              moreUnquoted        = None,
              unquotedShares      = Some(List(UnquotedShares(1000, 100, "Company A", "A"))),
              quotedShareAssets   = Some("Yes"),
              moreQuoted          = None,
              quotedShares        = Some(List(QuotedShares(2000, 200, "Company B", "B"))),
              propertyAsset       = Some("Yes"),
              moreProp            = None,
              propertyAssets      = Some(List(PropertyAssets(
                propertyAddress = Address(
                  addressLine1 = Some("6 Test Address"),
                  addressLine2 = Some("Test Street"),
                  addressLine3 = None,
                  addressLine4 = None,
                  addressLine5 = None,
                  ukPostCode   = Some("XX89 6YY"),
                  country      = Some("GB")
                ),
                propValue       = 300,
                propDescription = "Test Property"
              ))),
              otherAsset          = Some("Yes"),
              moreAsset           = None,
              otherAssets         = Some(List(OtherAssets(400, "Other Asset")))
            )
          )
        )
      )
    )
    SavedUserAnswers(referenceId, userAnswers, now)
  }

  "SaveForLaterRepository" - {

    "must save and retrieve simple record with encryption" in {
      val simpleSaved = SavedUserAnswers("ref-simple", AnswersData(None, None, None, None), now)
      repository.set(simpleSaved).futureValue mustBe true

      val retrieved = repository.get("ref-simple").futureValue.value
      retrieved.referenceId mustBe "ref-simple"
      retrieved.data        mustBe simpleSaved.data
      retrieved.lastUpdated mustBe now

      val raw = rawCollection.find().headOption().futureValue.value
      raw.get("data").get.asString().getValue must not include "AnswersData"
    }

    "must save and retrieve userAnswer JSON correctly" in {
      val saved = buildUserAnswer()
      repository.set(saved).futureValue mustBe true

      val retrieved = repository.get(saved.referenceId).futureValue.value
      retrieved.referenceId mustBe saved.referenceId
      retrieved.data        mustBe saved.data
      retrieved.lastUpdated mustBe now

      val propertyAsset = retrieved.data.transferDetails.value.typeOfAssets.value.propertyAssets.value.head.propertyAddress
      propertyAsset.addressLine1     mustBe Some("6 Test Address")
      propertyAsset.addressLine2     mustBe Some("Test Street")
      propertyAsset.ukPostCode.value mustBe "XX89 6YY"
    }

    "must produce different ciphertexts for the same userAnswer input" in {
      val saved1 = buildUserAnswer("ref1")
      val saved2 = buildUserAnswer("ref2")

      repository.set(saved1).futureValue mustBe true
      repository.set(saved2).futureValue mustBe true

      val rawDocs = rawCollection.find().collect().toFuture().futureValue
      val enc1    = rawDocs.find(_.get("referenceId").get.asString().getValue == "ref1").get.get("data").get.asString().getValue
      val enc2    = rawDocs.find(_.get("referenceId").get.asString().getValue == "ref2").get.get("data").get.asString().getValue

      enc1 must not be enc2
    }

    "must delete a record by referenceId" in {
      val saved = buildUserAnswer("ref-delete")
      repository.set(saved).futureValue          mustBe true
      repository.clear("ref-delete").futureValue mustBe true
      repository.get("ref-delete").futureValue   mustBe None
    }

    "must delete all records with clearAll" in {
      val saved = buildUserAnswer("ref-clearall")
      repository.set(saved).futureValue          mustBe true
      repository.clearAll().futureValue          mustBe Done
      repository.get("ref-clearall").futureValue mustBe None
    }

    "must return None when decryption fails for corrupted payload" in {
      val corruptDoc = Document(
        "referenceId" -> "ref-corrupt",
        "data"        -> "invalid-encryption",
        "lastUpdated" -> java.util.Date.from(now)
      )
      rawCollection.insertOne(corruptDoc).toFuture().futureValue
      repository.get("ref-corrupt").futureValue mustBe None
    }

    "must handle empty referenceId gracefully" in {
      val saved = buildUserAnswer("")
      repository.set(saved).futureValue                mustBe true
      repository.get("").futureValue.value.referenceId mustBe ""
      repository.clear("").futureValue                 mustBe true
      repository.get("").futureValue                   mustBe None
    }

    "must return None when decryption fails (mocked EncryptionService)" in {
      val mockEncryption = mock[EncryptionService]

      when(mockEncryption.encrypt(any[String])).thenAnswer((invocation: InvocationOnMock) => {
        val arg = invocation.getArgument(0, classOf[String])
        s"encrypted-$arg"
      })

      when(mockEncryption.decrypt(any[String])).thenReturn(Left(new RuntimeException("forced fail")))

      val repo = new SaveForLaterRepository(mongoComponent, mockEncryption, appConfig, mutableClock)

      val saved = buildUserAnswer("ref-fail")
      repo.set(saved).futureValue mustBe true

      repo.get("ref-fail").futureValue mustBe None
    }
  }
}
