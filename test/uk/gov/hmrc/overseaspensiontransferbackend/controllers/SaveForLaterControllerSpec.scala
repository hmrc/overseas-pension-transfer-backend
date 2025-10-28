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

package uk.gov.hmrc.overseaspensiontransferbackend.controllers

import org.apache.pekko.Done
import org.scalatest.freespec.AnyFreeSpec
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.services.SaveForLaterError.DeleteFailed
import uk.gov.hmrc.overseaspensiontransferbackend.services.{SaveForLaterError, SaveForLaterService}

import scala.concurrent.Future

class SaveForLaterControllerSpec extends AnyFreeSpec with SpecBase {

  private val routePrefix = "/overseas-pension-transfer-backend"

  "SaveForLaterController" - {

    "must return 200 with user answers if they exist" in {
      val mockService = mock[SaveForLaterService]

      when(mockService.getAnswers(eqTo(testId))(any))
        .thenReturn(Future.successful(Right(simpleUserAnswersDTO)))

      val app: Application =
        applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(GET, s"$routePrefix/save-for-later/${testId.value}")
        val result  = route(app, request).value

        status(result)        mustBe OK
        contentAsJson(result) mustBe Json.toJson(simpleUserAnswersDTO)
      }
    }

    "must return 404 if not found" in {
      val mockService = mock[SaveForLaterService]

      when(mockService.getAnswers(eqTo(testId))(any))
        .thenReturn(Future.successful(Left(SaveForLaterError.NotFound)))

      val app: Application =
        applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(GET, s"$routePrefix/save-for-later/${testId.value}")
        val result  = route(app, request).value

        status(result) mustBe NOT_FOUND
      }
    }

    "must return 204 NoContent if save is successful" in {
      val mockService = mock[SaveForLaterService]

      when(mockService.saveAnswer(eqTo(simpleUserAnswersDTO.copy(transferId = testId)))(any))
        .thenReturn(Future.successful(Right(simpleUserAnswersDTO)))

      val app: Application =
        applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(POST, s"$routePrefix/save-for-later")
          .withJsonBody(Json.toJson(simpleUserAnswersDTO))

        val result = route(app, request).value

        status(result)          mustBe NO_CONTENT
        contentAsString(result) mustBe empty
      }
    }

    "must return 400 BadRequest on transformation error" in {
      val mockService = mock[SaveForLaterService]

      when(mockService.saveAnswer(eqTo(simpleUserAnswersDTO.copy(transferId = testId)))(any))
        .thenReturn(Future.successful(Left(SaveForLaterError.TransformationError("something went wrong"))))

      val app: Application =
        applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(POST, s"$routePrefix/save-for-later")
          .withJsonBody(Json.toJson(simpleUserAnswersDTO))

        val result = route(app, request).value

        status(result)                             mustBe BAD_REQUEST
        (contentAsJson(result) \ "error").as[String] must include("Transformation failed")
      }
    }

    "must return 500 InternalServerError on save failure" in {
      val mockService = mock[SaveForLaterService]

      when(mockService.saveAnswer(eqTo(simpleUserAnswersDTO.copy(transferId = testId)))(any))
        .thenReturn(Future.successful(Left(SaveForLaterError.SaveFailed)))

      val app: Application =
        applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(POST, s"$routePrefix/save-for-later")
          .withJsonBody(Json.toJson(simpleUserAnswersDTO))

        val result = route(app, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "deleteAnswers" - {
      "Return No Content when a Right is returned from the service layer" in {
        val mockService = mock[SaveForLaterService]

        when(mockService.deleteAnswers(any)(any)).thenReturn(Future.successful(Right(Done)))

        val app: Application =
          applicationBuilder()
            .overrides(
              bind[SaveForLaterService].toInstance(mockService)
            )
            .build()

        running(app) {
          val request = FakeRequest(DELETE, s"$routePrefix/save-for-later/${testId.value}")

          val result = route(app, request).value

          status(result) mustBe NO_CONTENT
        }
      }

      "Return Internal Server Error when a Left is returned from the service layer" in {
        val mockService = mock[SaveForLaterService]

        when(mockService.deleteAnswers(any)(any)).thenReturn(Future.successful(Left(DeleteFailed)))

        val app: Application =
          applicationBuilder()
            .overrides(
              bind[SaveForLaterService].toInstance(mockService)
            )
            .build()

        running(app) {
          val request = FakeRequest(DELETE, s"$routePrefix/save-for-later/${testId.value}")

          val result = route(app, request).value

          status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
