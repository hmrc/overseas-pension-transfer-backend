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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import play.api.test.FakeRequest
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.services.SaveForLaterService

import scala.concurrent.Future

class SaveForLaterControllerSpec extends AnyFreeSpec with SpecBase with GuiceOneAppPerSuite {

  private val routePrefix = "/overseas-pension-transfer-backend"

  "SaveForLaterController" - {

    "getAnswers should return 200 with user answers if they exist" in {
      val mockService = mock[SaveForLaterService]

      when(mockService.getAnswers(eqTo(testId))(any))
        .thenReturn(Future.successful(Some(simpleSavedUserAnswers)))

      val app: Application =
        applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(GET, s"$routePrefix/save-for-later/$testId")
        val result  = route(app, request).value

        status(result) mustEqual OK
        val json = contentAsJson(result)
        (json \ "data").as[JsObject] mustBe Json.obj("field" -> "value")
      }
    }

    "getAnswers should return 404 if no answers exist" in {
      val mockService = mock[SaveForLaterService]
      when(mockService.getAnswers(eqTo(testId))(any))
        .thenReturn(Future.successful(None))

      val app: Application =
        applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(GET, s"$routePrefix/save-for-later/$testId")
        val result  = route(app, request).value

        status(result) mustBe NOT_FOUND
      }
    }

    "saveAnswers should return 201 Created if save successful" in {
      val mockService = mock[SaveForLaterService]
      val expected    = simpleSavedUserAnswers

      when(mockService.saveAnswer(eqTo(simpleUserAnswersDTO.copy(referenceId = testId)))(any))
        .thenReturn(Future.successful(Some(expected)))

      val app: Application =
        applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(POST, s"$routePrefix/save-for-later/$testId")
          .withJsonBody(Json.toJson(simpleUserAnswersDTO))

        val result = route(app, request).value

        status(result) mustBe CREATED
        val json = contentAsJson(result)
        (json \ "data").as[JsObject] mustBe Json.obj("field" -> "value")
      }
    }

    "saveAnswers should return 500 InternalServerError if save fails" in {
      val mockService = mock[SaveForLaterService]

      when(mockService.saveAnswer(eqTo(simpleUserAnswersDTO.copy(referenceId = testId)))(any))
        .thenReturn(Future.successful(None))

      val app: Application =
        applicationBuilder()
          .overrides(bind[SaveForLaterService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(POST, s"$routePrefix/save-for-later/$testId")
          .withJsonBody(Json.toJson(simpleUserAnswersDTO))

        val result = route(app, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
