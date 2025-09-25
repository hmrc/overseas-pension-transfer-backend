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

import org.apache.pekko.Done
import org.scalatest.freespec.AnyFreeSpec
import play.api.libs.json.{JsError, JsObject, Json}
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.models.{AnswersData, PstrNumber, SavedUserAnswers, TransferringMember}
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.services.SaveForLaterError.DeleteFailed
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.UserAnswersTransformer
import uk.gov.hmrc.overseaspensiontransferbackend.utils.CountryCodeReader

import scala.concurrent.Future

class SaveForLaterServiceSpec extends AnyFreeSpec with SpecBase {

  private val mockRepository  = mock[SaveForLaterRepository]
  private val mockTransformer = mock[UserAnswersTransformer]
  private val service         = new SaveForLaterServiceImpl(mockRepository, mockTransformer)

  private val validData: JsObject = Json.obj(
    "memberDetails" -> Json.obj(
      "memberName" -> Json.obj(
        "firstName" -> "Foo",
        "lastName"  -> "Bar"
      )
    )
  )

  private val validSaved = SavedUserAnswers(
    referenceId = testId,
    pstr        = pstr,
    data        = AnswersData(None, Some(TransferringMember(None)), None, None),
    lastUpdated = now
  )

  private val validDTO = UserAnswersDTO(testId, pstr, validData, now)

  "SaveForLaterServiceSpec" - {

    "must return Right(UserAnswersDTO) when data exists and deconstruct succeeds" in {
      when(mockRepository.get(testId)).thenReturn(Future.successful(Some(validSaved)))
      when(mockTransformer.deconstruct(*)).thenReturn(Right(validData))

      val result = service.getAnswers(testId)

      result.futureValue mustBe Right(validDTO)
    }

    "must return Left(NotFound) when no data exists" in {
      when(mockRepository.get(testId)).thenReturn(Future.successful(None))

      val result = service.getAnswers(testId)

      result.futureValue mustBe Left(SaveForLaterError.NotFound)
    }

    "must return Left(TransformationError) when deconstruct fails" in {
      when(mockRepository.get(testId)).thenReturn(Future.successful(Some(validSaved)))
      when(mockTransformer.deconstruct(*)).thenReturn(Left(JsError("deconstruct failed")))

      val result = service.getAnswers(testId)

      result.futureValue match {
        case Left(SaveForLaterError.TransformationError(msg)) => msg must include("deconstruct failed")
        case other                                            => fail(s"Unexpected result: $other")
      }
    }

    "must save new answers if valid" in {
      when(mockRepository.get(testId)).thenReturn(Future.successful(None))
      when(mockTransformer.construct(*)).thenReturn(Right(validData))
      when(mockRepository.set(*)).thenReturn(Future.successful(true))

      val result = service.saveAnswer(validDTO)

      result.futureValue mustBe Right(())
    }

    "must return Left(SaveFailed) if repo.set returns false" in {
      when(mockRepository.get(testId)).thenReturn(Future.successful(None))
      when(mockTransformer.construct(*)).thenReturn(Right(validData))
      when(mockRepository.set(*)).thenReturn(Future.successful(false))

      val result = service.saveAnswer(validDTO)

      result.futureValue mustBe Left(SaveForLaterError.SaveFailed)
    }

    "must merge with existing data before save" in {
      when(mockRepository.get(testId)).thenReturn(Future.successful(Some(validSaved)))
      when(mockTransformer.construct(*)).thenReturn(Right(validData))
      when(mockRepository.set(*)).thenReturn(Future.successful(true))

      val result = service.saveAnswer(validDTO)

      result.futureValue mustBe Right(())
    }

    "must return Left(TransformationError) when construct fails" in {
      when(mockTransformer.construct(*)).thenReturn(Left(JsError("construct failed")))

      val result = service.saveAnswer(validDTO)

      result.futureValue match {
        case Left(SaveForLaterError.TransformationError(msg)) => msg must include("construct failed")
        case other                                            => fail(s"Unexpected result: $other")
      }
    }

    "must return Left(TransformationError) when a known field is malformed" in {
      val malformedJson = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "nino" -> 12345
          )
        )
      )

      when(mockTransformer.construct(*)).thenReturn(Right(malformedJson))
      when(mockRepository.get(testId)).thenReturn(Future.successful(Some(validSaved)))
      when(mockRepository.set(*)).thenReturn(Future.successful(true))

      val result = service.saveAnswer(validDTO)

      result.futureValue match {
        case Left(SaveForLaterError.TransformationError(msg)) => msg.toLowerCase must include("nino")
        case other                                            => fail(s"Expected TransformationError due to malformed field, got: $other")
      }
    }

    "deleteAnswers" - {
      "Return a Right(Done) when repository returns true" in {
        when(mockRepository.clear(*)).thenReturn(Future.successful(true))

        val result = service.deleteAnswers(testId)

        result.futureValue mustBe Right(Done)
      }

      "Return a Left(DeleteFailed) when repository returns false" in {
        when(mockRepository.clear(*)).thenReturn(Future.successful(false))

        val result = service.deleteAnswers(testId)

        result.futureValue mustBe Left(DeleteFailed)
      }
    }
  }
}
