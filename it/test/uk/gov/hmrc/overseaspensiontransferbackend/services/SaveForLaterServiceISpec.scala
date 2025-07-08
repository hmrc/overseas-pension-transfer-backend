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

package uk.gov.hmrc.overseaspensiontransferbackend.services

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.base.BaseISpec
import uk.gov.hmrc.overseaspensiontransferbackend.models.{AnswersData, SavedUserAnswers}
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository

import java.time.Instant
import java.util.UUID

class SaveForLaterServiceISpec extends BaseISpec {

  lazy val repository: SaveForLaterRepository = app.injector.instanceOf[SaveForLaterRepository]
  lazy val service: SaveForLaterService       = app.injector.instanceOf[SaveForLaterService]

  "SaveForLaterService" should {

    "transform and persist data correctly" in {
      val id      = UUID.randomUUID().toString
      val now     = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)
      val rawJson = Json.parse(
        """
        {
          "memberDetails": {
            "name": { "firstName": "Test", "lastName": "McTest" },
            "nino": "AB123456B",
            "dateOfBirth": "2011-06-05",
            "principalResAddDetails": {
              "addressLine1": "1",
              "addressLine2": "Test road",
              "addressLine3": "Testville",
              "addressLine4": "East Testerly",
              "country": { "code": "AE-AZ", "name": "Abu Dhabi" },
              "ukPostCode": "ab123c",
              "poBoxNumber": "baa"
            },
            "memUkResident": false,
            "memEverUkResident": true,
            "lastPrincipalAddDetails": {
              "addressLine1": "Flat 2",
              "addressLine2": "7 Other Place",
              "addressLine3": "Some District",
              "ukPostCode": "ZZ1 1ZZ"
            },
            "dateMemberLeftUk": "2011-06-06"
          }
        }
        """
      ).as[JsObject]

      val dto = UserAnswersDTO(id, rawJson, now)

      await(service.saveAnswer(dto)) shouldBe Right(())

      val result = await(repository.get(id)).value

      result.referenceId shouldBe id
      result.lastUpdated shouldBe now

      val json = Json.toJsObject(result.data)

      val memberDetailsPath = json \ "transferringMember" \ "memberDetails"

      (memberDetailsPath \ "foreName").as[String]    shouldBe "Test"
      (memberDetailsPath \ "lastName").as[String]    shouldBe "McTest"
      (memberDetailsPath \ "nino").as[String]        shouldBe "AB123456B"
      (memberDetailsPath \ "dateOfBirth").as[String] shouldBe "2011-06-05"

      val principalAddress = memberDetailsPath \ "principalResAddDetails" \ "addressDetails"
      (principalAddress \ "addressLine1").as[String]     shouldBe "1"
      (principalAddress \ "addressLine2").as[String]     shouldBe "Test road"
      (principalAddress \ "addressLine3").as[String]     shouldBe "Testville"
      (principalAddress \ "addressLine4").as[String]     shouldBe "East Testerly"
      (principalAddress \ "ukPostCode").as[String]       shouldBe "ab123c"
      (principalAddress \ "country" \ "code").as[String] shouldBe "AE-AZ"
      (principalAddress \ "country" \ "name").as[String] shouldBe "Abu Dhabi"

      (memberDetailsPath \ "principalResAddDetails" \ "poBoxNumber").as[String] shouldBe "baa"

      val residency = memberDetailsPath \ "memberResidencyDetails"
      (residency \ "memUkResident").as[String]     shouldBe "No"
      (residency \ "memEverUkResident").as[String] shouldBe "Yes"

      val lastUK     = residency \ "lastPrincipalAddDetails"
      val lastUKAddr = lastUK \ "addressDetails"
      (lastUKAddr \ "addressLine1").as[String] shouldBe "Flat 2"
      (lastUKAddr \ "addressLine2").as[String] shouldBe "7 Other Place"
      (lastUKAddr \ "addressLine3").as[String] shouldBe "Some District"
      (lastUKAddr \ "ukPostCode").as[String]   shouldBe "ZZ1 1ZZ"

      (lastUK \ "dateMemberLeftUk").as[String] shouldBe "2011-06-06"
    }

    "retrieve and transform data" in {
      val id  = UUID.randomUUID().toString
      val now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)

      val preTransformedJson = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "foreName"               -> "GetOnly",
            "lastName"               -> "McLoad",
            "nino"                   -> "GG123456G",
            "dateOfBirth"            -> "2012-12-12",
            "principalResAddDetails" -> Json.obj(
              "addressDetails" -> Json.obj(
                "addressLine1" -> "1 Get St",
                "addressLine2" -> "Loadtown",
                "addressLine3" -> "Getshire",
                "addressLine4" -> "East Loadonia",
                "addressLine5" -> "Zone 9",
                "ukPostCode"   -> "LO4 4AD",
                "country"      -> Json.obj("code" -> "IE", "name" -> "Ireland")
              ),
              "poBoxNumber"    -> "PO456"
            ),
            "memberResidencyDetails" -> Json.obj(
              "memUkResident"           -> "No",
              "memEverUkResident"       -> "Yes",
              "lastPrincipalAddDetails" -> Json.obj(
                "addressDetails"   -> Json.obj(
                  "addressLine1" -> "Flat 8",
                  "addressLine2" -> "Memory Lane",
                  "addressLine3" -> "Recall District",
                  "addressLine4" -> "Echoes",
                  "ukPostCode"   -> "ZZ2 2ZZ",
                  "country"      -> Json.obj("code" -> "UK", "name" -> "United Kingdom")
                ),
                "dateMemberLeftUk" -> "2013-05-01"
              )
            )
          )
        )
      )

      val savedAnswers = SavedUserAnswers(id, preTransformedJson.as[AnswersData], now)

      await(repository.set(savedAnswers)) shouldBe true

      val result = await(service.getAnswers(id))

      result match {
        case Right(dto) =>
          dto.referenceId shouldBe id
          dto.lastUpdated shouldBe now

          val frontend = dto.data

          val details = frontend \ "memberDetails"
          (details \ "name" \ "firstName").as[String] shouldBe "GetOnly"
          (details \ "name" \ "lastName").as[String]  shouldBe "McLoad"
          (details \ "nino").as[String]               shouldBe "GG123456G"
          (details \ "dateOfBirth").as[String]        shouldBe "2012-12-12"

          val principal = details \ "principalResAddDetails"
          (principal \ "addressLine1").as[String]     shouldBe "1 Get St"
          (principal \ "addressLine2").as[String]     shouldBe "Loadtown"
          (principal \ "addressLine3").as[String]     shouldBe "Getshire"
          (principal \ "addressLine4").as[String]     shouldBe "East Loadonia"
          (principal \ "addressLine5").as[String]     shouldBe "Zone 9"
          (principal \ "ukPostCode").as[String]       shouldBe "LO4 4AD"
          (principal \ "country" \ "code").as[String] shouldBe "IE"
          (principal \ "country" \ "name").as[String] shouldBe "Ireland"
          (principal \ "poBoxNumber").as[String]      shouldBe "PO456"

          val residency = details \ "memUkResident"
          residency.as[Boolean] shouldBe false

          val everUk = details \ "memEverUkResident"
          everUk.as[Boolean] shouldBe true

          val lastUK = details \ "lastPrincipalAddDetails"
          (lastUK \ "addressLine1").as[String]     shouldBe "Flat 8"
          (lastUK \ "addressLine2").as[String]     shouldBe "Memory Lane"
          (lastUK \ "addressLine3").as[String]     shouldBe "Recall District"
          (lastUK \ "addressLine4").as[String]     shouldBe "Echoes"
          (lastUK \ "ukPostCode").as[String]       shouldBe "ZZ2 2ZZ"
          (lastUK \ "country" \ "code").as[String] shouldBe "UK"
          (lastUK \ "country" \ "name").as[String] shouldBe "United Kingdom"
          (lastUK \ "dateMemberLeftUk").as[String] shouldBe "2013-05-01"

        case Left(err) =>
          fail(s"Expected successful result but got error: $err")
      }
    }
  }
}
