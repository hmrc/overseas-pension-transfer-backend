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
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.models.Pstr
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.{TransferDeconstructionError, TransferNotFound}
import uk.gov.hmrc.overseaspensiontransferbackend.services.SubmissionService

import scala.concurrent.Future

class GetTransferDataControllerSpec extends AnyFreeSpec with Matchers with SpecBase {

  private val mockSubmissionService: SubmissionService = mock[SubmissionService]

  def application: Application = GuiceApplicationBuilder().overrides(
    bind[SubmissionService].toInstance(mockSubmissionService)
  ).build()

  "getTransfer" - {
    "Return 200 with Json when service returns Right UserAnswersDTO" in {
      val json = Json.obj("key" -> "value")

      when(mockSubmissionService.getTransfer(any)(any))
        .thenReturn(Future.successful(Right(UserAnswersDTO("id", pstr, json, now))))

      running(application) {
        val request = FakeRequest(GET, "/overseas-pension-transfer-backend/get-transfer/transfer-id?pstr=12345678AB&qtStatus=InProgress")

        val result = route(application, request).value

        status(result)        mustBe OK
        contentAsJson(result) mustBe Json.obj(
          "referenceId" -> "id",
          "pstr"        -> "12345678AB",
          "data"        -> Json.obj("key" -> "value"),
          "lastUpdated" -> now
        )
      }
    }

    "return 404 when service returns Left TransferNotFound" in {
      when(mockSubmissionService.getTransfer(any)(any))
        .thenReturn(Future.successful(Left(TransferNotFound("No Transfer found"))))

      running(application) {
        val request = FakeRequest(GET, "/overseas-pension-transfer-backend/get-transfer/transfer-id?pstr=12345678AB&qtStatus=InProgress")

        val result = route(application, request).value

        status(result) mustBe NOT_FOUND
      }
    }

    "return 500 when service returns Left TransferDeconstructionError" in {
      when(mockSubmissionService.getTransfer(any)(any))
        .thenReturn(Future.successful(Left(TransferDeconstructionError("Error"))))

      running(application) {
        val request = FakeRequest(GET, "/overseas-pension-transfer-backend/get-transfer/transfer-id?pstr=12345678AB&qtStatus=InProgress")

        val result = route(application, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
