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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.models.{AnswersData, MemberDetails, SavedUserAnswers, TransferringMember}
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.UserAnswersTransformer

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class SaveForLaterServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar with ScalaFutures {

  implicit val ec: ExecutionContext           = scala.concurrent.ExecutionContext.Implicits.global
  implicit val hc: HeaderCarrier              = HeaderCarrier()
  val mockRepository: SaveForLaterRepository  = mock[SaveForLaterRepository]
  val mockTransformer: UserAnswersTransformer = mock[UserAnswersTransformer]
  val service                                 = new SaveForLaterServiceImpl(mockRepository, mockTransformer)

  private val now = Instant.parse("2025-06-29T12:00:00Z")

  private val validData: JsObject = Json.obj("memberDetails" -> Json.obj("memberName" -> Json.obj("firstName" -> "Foo", "lastName" -> "Bar")))
  private val validSaved          = SavedUserAnswers("ref-1", AnswersData(Some(TransferringMember(None)), None, None, None), now)
  private val validDTO            = UserAnswersDTO("ref-1", validData, now)

  "getAnswers" - {

    "should return Right(UserAnswersDTO) when data exists and enrich succeeds" in {
      when(mockRepository.get("ref-1")).thenReturn(Future.successful(Some(validSaved)))
      when(mockTransformer.applyEnrichTransforms(any())).thenReturn(Right(validData))

      val result = service.getAnswers("ref-1")

      result.futureValue mustBe Right(UserAnswersDTO("ref-1", validData, now))
    }

    "should return Left(NotFound) when no data exists" in {
      when(mockRepository.get("ref-1")).thenReturn(Future.successful(None))
      when(mockTransformer.applyEnrichTransforms(any())).thenReturn(Right(validData))

      val result = service.getAnswers("ref-1")

      result.futureValue mustBe Left(SaveForLaterError.NotFound)
    }
  }

  "saveAnswer" - {

    "should save new answers if valid" in {
      when(mockRepository.get("ref-1")).thenReturn(Future.successful(None))
      when(mockTransformer.applyCleanseTransforms(any())).thenReturn(Right(validData))
      when(mockRepository.set(any())).thenReturn(Future.successful(true))

      val result = service.saveAnswer(validDTO)

      result.futureValue mustBe Right()
    }

    "should return Left(SaveFailed) if repo.set returns false" in {
      when(mockRepository.get("ref-1")).thenReturn(Future.successful(None))
      when(mockTransformer.applyCleanseTransforms(any())).thenReturn(Right(validData))
      when(mockRepository.set(any())).thenReturn(Future.successful(false))

      val result = service.saveAnswer(validDTO)

      result.futureValue mustBe Left(SaveForLaterError.SaveFailed)
    }

    "should merge with existing data before save" in {
      val existingData = Json.obj("memberDetails" -> Json.obj("memberNino" -> "QQ123456A"))
      val existing     = SavedUserAnswers("ref-1", AnswersData(Some(TransferringMember(None)), None, None, None), now)
      when(mockRepository.get("ref-1")).thenReturn(Future.successful(Some(existing)))
      when(mockTransformer.applyCleanseTransforms(any())).thenReturn(Right(validData))
      when(mockRepository.set(any())).thenReturn(Future.successful(true))

      val result = service.saveAnswer(validDTO)

      result.futureValue mustBe Right()
    }
  }
}
