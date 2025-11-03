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
import uk.gov.hmrc.overseaspensiontransferbackend.base.{BaseISpec, UserAnswersTestData}
import uk.gov.hmrc.overseaspensiontransferbackend.models.{AnswersData, PstrNumber, SavedUserAnswers}
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository

class SaveForLaterServiceISpec extends BaseISpec {

  lazy val repository: SaveForLaterRepository = app.injector.instanceOf[SaveForLaterRepository]
  lazy val service: SaveForLaterService       = app.injector.instanceOf[SaveForLaterService]

  private lazy val id  = freshId()
  private val pstr = PstrNumber("12345678AB")

  "SaveForLaterService" - {

    "transform and persist full data correctly" in {

      val rawJson = UserAnswersTestData.fullUserAnswersExternalJson

      val dto = withSavedDto(id, pstr, rawJson, fixedInstant)

      await(service.saveAnswer(dto)) mustBe Right(())

      val result = await(repository.get(id.value)).value
      result.transferId mustBe id
      result.pstr mustBe pstr
      result.lastUpdated mustBe fixedInstant

      result mustBe SavedUserAnswers(id, pstr, UserAnswersTestData.fullUserAnswersInternalJson.as[AnswersData], fixedInstant)

    }

    "retrieve and transform full data" in {
      val savedAnswers = SavedUserAnswers(id, pstr, UserAnswersTestData.fullUserAnswersInternalJson.as[AnswersData], fixedInstant)

      await(repository.set(savedAnswers)) mustBe true

      val result = await(service.getAnswers(id))

      result match {
        case Right(dto) =>
          dto.data mustBe UserAnswersTestData.fullUserAnswersExternalJson
        case Left(err) =>
          fail(s"Expected successful result but got error: $err")
      }
    }

    "save user answers into internal format and return as external format" in {
      val rawJson = UserAnswersTestData.fullUserAnswersExternalJson

      val dto = withSavedDto(id, pstr, rawJson, fixedInstant)

      await(service.saveAnswer(dto)) mustBe Right(())

      val result = await(service.getAnswers(id))

      result match {
        case Right(dto) =>

          dto.transferId mustBe id
          dto.pstr mustBe pstr
          dto.lastUpdated mustBe fixedInstant
          dto.data mustEqual UserAnswersTestData.fullUserAnswersExternalJson
        case Left(err) =>
          fail(s"Expected successful result but got error: $err")
      }
    }

    "incrementally add new data while maintaining previously added data" in {
      await(service.saveAnswer(withSavedDto(id, pstr, UserAnswersTestData.memberDetailsExternalJson, fixedInstant)))               mustBe Right(())
      await(service.saveAnswer(withSavedDto(id, pstr, UserAnswersTestData.qropsDetailsExternalJson, fixedInstant.plusSeconds(1)))) mustBe Right(())

      val result = await(service.getAnswers(id))

      result match {
        case Right(dto) =>
          val memberDetailsExpected = (UserAnswersTestData.memberDetailsExternalJson \ "memberDetails").as[JsObject]
          val memberDetailsActual = (dto.data \ "memberDetails").as[JsObject]

          memberDetailsExpected mustBe memberDetailsActual

          val qropsDetails = dto.data \ "qropsDetails"

          assertJson(
            qropsDetails,
            Map(
              "qropsFullName" -> "Test Scheme"
            )
          )
      }
    }

    "transform and persist qropsEstablished correctly" in {
      val rawJson = UserAnswersTestData.memberDetailsExternalJson
        .deepMerge(UserAnswersTestData.qropsDetailsEstablishedExternalJson)

      val dto = withSavedDto(id, pstr, rawJson, fixedInstant)

      await(service.saveAnswer(dto)) mustBe Right(())

      val result = await(repository.get(id.value)).value
      result.transferId mustBe id
      result.pstr mustBe pstr
      result.lastUpdated mustBe fixedInstant

      val expectedInternal = UserAnswersTestData.transferringMemberInternalJson
        .deepMerge(UserAnswersTestData.qropsDetailsEstablishedInternalJson)

      result mustBe SavedUserAnswers(id, pstr, expectedInternal.as[AnswersData], fixedInstant)
    }

    "transform and persist qropsEstablishedOther correctly" in {
      val rawJson = UserAnswersTestData.memberDetailsExternalJson
        .deepMerge(UserAnswersTestData.qropsDetailsEstablishedOtherExternalJson)

      val dto = withSavedDto(id, pstr, rawJson, fixedInstant)

      await(service.saveAnswer(dto)) mustBe Right(())

      val result = await(repository.get(id.value)).value
      result.transferId mustBe id
      result.pstr mustBe pstr
      result.lastUpdated mustBe fixedInstant

      val expectedInternal = UserAnswersTestData.transferringMemberInternalJson
        .deepMerge(UserAnswersTestData.qropsDetailsEstablishedOtherInternalJson)

      result mustBe SavedUserAnswers(id, pstr, expectedInternal.as[AnswersData], fixedInstant)
    }

  }
}
