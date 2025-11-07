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

import org.mockito.ArgumentCaptor
import org.scalatest.freespec.AnyFreeSpec
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication.{PsaId, PspId}
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.{PsaSubmissionDTO, PspSubmissionDTO}
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer._
import uk.gov.hmrc.overseaspensiontransferbackend.services.TransferService

import java.time.Instant
import scala.concurrent.Future

class SubmissionControllerSpec
    extends AnyFreeSpec
    with SpecBase {

  private val routePrefix = "/overseas-pension-transfer-backend"
  private val testRefId   = "ref-123"

  "SubmissionController.submitTransfer" - {

    "must normalise a PSA submission and return 200 with SubmissionResponse" in {
      val mockService = mock[TransferService]

      val psaDto           = PsaSubmissionDTO(
        referenceId = TransferNumber("to-be-overridden"),
        userId      = PsaId("A1234567"),
        lastUpdated = now
      )
      val psaJson: JsValue = Json.toJson(psaDto)

      val expectedResponse = SubmissionResponse(QtNumber("QT123456"), Instant.now)

      when(mockService.submitTransfer(any[NormalisedSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(expectedResponse)))

      val app: Application =
        applicationBuilder()
          .overrides(bind[TransferService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(POST, s"$routePrefix/submit-declaration/$testRefId")
          .withJsonBody(psaJson)

        val result = route(app, fakeIdentifierRequest(request)).value

        status(result)        mustBe OK
        contentAsJson(result) mustBe Json.toJson(expectedResponse)

        val captor: ArgumentCaptor[NormalisedSubmission] =
          ArgumentCaptor.forClass(classOf[NormalisedSubmission])

        verify(mockService).submitTransfer(captor.capture())(any[HeaderCarrier])
        val captured = captor.getValue

        captured.referenceId          mustBe TransferNumber(testRefId)
        captured.userId               mustBe PsaId("A1234567")
        captured.maybeAssociatedPsaId mustBe None
        captured.lastUpdated          mustBe now
      }
    }

    "must normalise a PSP submission and return 200 with SubmissionResponse" in {
      val mockService = mock[TransferService]

      val pspDto           = PspSubmissionDTO(
        referenceId = TransferNumber("to-be-overridden"),
        userId      = PspId("X9999999"),
        psaId       = PsaId("A7654321"),
        lastUpdated = now
      )
      val pspJson: JsValue = Json.toJson(pspDto)

      val expectedResponse = SubmissionResponse(QtNumber("QT999999"), Instant.now)

      when(mockService.submitTransfer(any[NormalisedSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(expectedResponse)))

      val app: Application =
        applicationBuilder()
          .overrides(bind[TransferService].toInstance(mockService))
          .build()

      running(app) {
        val request = FakeRequest(POST, s"$routePrefix/submit-declaration/$testRefId")
          .withJsonBody(pspJson)

        val result = route(app, fakeIdentifierRequest(request)).value

        status(result)        mustBe OK
        contentAsJson(result) mustBe Json.toJson(expectedResponse)

        val captor: ArgumentCaptor[NormalisedSubmission] =
          ArgumentCaptor.forClass(classOf[NormalisedSubmission])

        verify(mockService).submitTransfer(captor.capture())(any[HeaderCarrier])
        val captured = captor.getValue

        captured.referenceId          mustBe TransferNumber(testRefId)
        captured.userId               mustBe PspId("X9999999")
        captured.maybeAssociatedPsaId mustBe Some(PsaId("A7654321"))
        captured.lastUpdated          mustBe now
      }
    }

    "must return 400 with error JSON when service returns TransformationError" in {
      val mockService = mock[TransferService]

      when(mockService.submitTransfer(any[NormalisedSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(SubmissionTransformationError("bad data"))))

      val app: Application =
        applicationBuilder()
          .overrides(bind[TransferService].toInstance(mockService))
          .build()

      val payload: JsValue = Json.toJson(
        PsaSubmissionDTO(TransferNumber("ignored"), userId = PsaId("A1234567"), lastUpdated = now)
      )

      running(app) {
        val request = FakeRequest(POST, s"$routePrefix/submit-declaration/$testRefId")
          .withJsonBody(payload)

        val result = route(app, fakeIdentifierRequest(request)).value

        status(result)                               mustBe BAD_REQUEST
        (contentAsJson(result) \ "error").as[String] mustBe "Transformation failed"
        (contentAsJson(result) \ "details").as[String] must include("bad data")
      }
    }

    "must return 500 when service returns SubmissionFailed" in {
      val mockService = mock[TransferService]

      when(mockService.submitTransfer(any[NormalisedSubmission])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(SubmissionFailed)))

      val app: Application =
        applicationBuilder()
          .overrides(bind[TransferService].toInstance(mockService))
          .build()

      val payload: JsValue = Json.toJson(
        PspSubmissionDTO(TransferNumber("ignored"), userId = PspId("X9999999"), psaId = PsaId("A7654321"), lastUpdated = now)
      )

      running(app) {
        val request = FakeRequest(POST, s"$routePrefix/submit-declaration/$testRefId")
          .withJsonBody(payload)

        val result = route(app, fakeIdentifierRequest(request)).value

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
