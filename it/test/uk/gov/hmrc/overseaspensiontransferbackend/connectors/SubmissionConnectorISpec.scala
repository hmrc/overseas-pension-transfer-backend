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

package uk.gov.hmrc.overseaspensiontransferbackend.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status._
import play.api.inject
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, RequestId}
import uk.gov.hmrc.overseaspensiontransferbackend.base.BaseISpec
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream.HipOriginFailures.Failure
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream._
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.QtNumber
import uk.gov.hmrc.overseaspensiontransferbackend.models.{AnswersData, Pstr, QtDetails, SavedUserAnswers, Submitted}
import uk.gov.hmrc.overseaspensiontransferbackend.validators.ValidatedSubmission

import java.time.Instant

class SubmissionConnectorISpec extends BaseISpec {

  override protected def moduleOverrides: Seq[GuiceableModule] =
    Seq(
      inject.bind[SubmissionConnector].to[SubmissionConnectorImpl]
    )

  private val now = Instant.now()
  private val answersData = AnswersData(None, None, None, None)
  private val savedUserAnswers = SavedUserAnswers("", answersData, now)

  private val connector = app.injector.instanceOf[SubmissionConnector]

  "submit" - {
    "return an exception when X-Request-Id is missing" in {
      intercept[Exception](connector.submit(ValidatedSubmission(savedUserAnswers))).getMessage mustBe
        "Header X-Request-ID missing"
    }

    "send correlationId as header when RequestId is present" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("id")))

      connector.submit(ValidatedSubmission(savedUserAnswers))

      verify(postRequestedFor(urlEqualTo("/RESTAdapter/pods/reports/qrops-transfer"))
        .withHeader("correlationid", equalTo("id"))
      )
    }

    "return DownstreamSuccess when 201 and valid payload is returned from downstream" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("id")))

      val downstreamPayload = Json.obj(
        "success" -> Json.obj(
          "qtReference" -> "QT123456",
          "processingDate" -> now.toString,
          "formBundleNumber" -> "123"
        )
      ).toString()

      stubPost("/RESTAdapter/pods/reports/qrops-transfer", downstreamPayload, CREATED)

      val result: Either[DownstreamError, DownstreamSuccess] = await(connector.submit(ValidatedSubmission(savedUserAnswers)))

      result mustBe Right(DownstreamSuccess(QtNumber("QT123456"), now, "123"))
    }

    "return HipBadRequest when 400 is returned with valid payload" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("id")))

      val downstreamPayload = Json.obj(
        "origin" -> "HIP",
        "response" -> Json.obj(
          "error" -> Json.obj(
            "code" -> "code",
            "message" -> "There's been an error",
            "logID" -> "logID"
          )
        )
      ).toString()

      stubPost("/RESTAdapter/pods/reports/qrops-transfer", downstreamPayload, BAD_REQUEST)

      val result: Either[DownstreamError, DownstreamSuccess] = await(connector.submit(ValidatedSubmission(savedUserAnswers)))

      result mustBe Left(HipBadRequest("HIP", "code", "There's been an error", Some("logID")))
    }

    "return EtmpValidationError when 422 is returned with valid payload" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("id")))

      val downstreamPayload = Json.obj(
        "errors" -> Json.obj(
          "processingDate" -> now,
          "code" -> "003",
          "text" -> "Request could not be processed"
        )
      ).toString()

      stubPost("/RESTAdapter/pods/reports/qrops-transfer", downstreamPayload, UNPROCESSABLE_ENTITY)

      val result: Either[DownstreamError, DownstreamSuccess] = await(connector.submit(ValidatedSubmission(savedUserAnswers)))

      result mustBe Left(EtmpValidationError(now.toString, "003", "Request could not be processed"))
    }

    "return HipBadRequest when 500 is returned with valid payload" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("id")))

      val downstreamPayload = Json.obj(
        "origin" -> "HoD",
        "response" -> Json.obj(
          "error" -> Json.obj(
            "code" -> "code",
            "message" -> "There's been an error",
            "logID" -> "logID"
          )
        )
      ).toString()

      stubPost("/RESTAdapter/pods/reports/qrops-transfer", downstreamPayload, INTERNAL_SERVER_ERROR)

      val result: Either[DownstreamError, DownstreamSuccess] = await(connector.submit(ValidatedSubmission(savedUserAnswers)))

      result mustBe Left(HipBadRequest("HoD", "code", "There's been an error", Some("logID")))
    }

    "return HipOriginFailures when 503 is returned with valid payload" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("id")))

      val downstreamPayload = Json.obj(
        "origin" -> "HoD",
        "response" -> Json.obj(
          "failures" -> Seq(
            Json.obj(
              "type" -> "type",
              "reason" -> "reason"
            )
          )
        )
      ).toString()

      stubPost("/RESTAdapter/pods/reports/qrops-transfer", downstreamPayload, SERVICE_UNAVAILABLE)

      val result: Either[DownstreamError, DownstreamSuccess] = await(connector.submit(ValidatedSubmission(savedUserAnswers)))

      result mustBe Left(HipOriginFailures("HoD", List(Failure("type", "reason"))))
    }
  }

  "getTransfer" - {
    "return Right DownstreamTransferData when 200 and valid data is returned from downstream" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("id")))

      val downstreamPayload = Json.obj(
        "success" -> Json.obj(
          "pstr" -> "12345678AB",
          "qtDetails" -> Json.obj(
            "qtVersion" -> "001",
            "qtStatus" -> "Submitted",
            "receiptDate" -> now,
            "qtReference" -> "QT123456"
          )
        )
      ).toString()

      stubGet("/etmp/RESTAdapter/pods/reports/qrops-transfer?pstr=12345678AB&qtNumber=QT123456&versionNumber=001", downstreamPayload)

      val result: Either[DownstreamError, DownstreamTransferData] = await(connector.getTransfer("12345678AB", Some("QT123456"), Some("001")))

      result mustBe Right(
        DownstreamTransferData(
          Pstr("12345678AB"),
          QtDetails(
            "001",
            Submitted,
            now,
            QtNumber("QT123456"),
            None,
            None
          ),
          None,
          None,
          None
        )
      )
    }

    "return HipBadRequest when 400 is returned with valid payload" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("id")))

      val downstreamPayload = Json.obj(
        "origin" -> "HIP",
        "response" -> Json.obj(
          "error" -> Json.obj(
            "code" -> "code",
            "message" -> "There's been an error",
            "logID" -> "logID"
          )
        )
      ).toString()

      stubGet("/etmp/RESTAdapter/pods/reports/qrops-transfer?pstr=12345678AB&qtNumber=QT123456&versionNumber=001", downstreamPayload, BAD_REQUEST)

      val result: Either[DownstreamError, DownstreamTransferData] = await(connector.getTransfer("12345678AB", Some("QT123456"), Some("001")))

      result mustBe Left(HipBadRequest("HIP", "code", "There's been an error", Some("logID")))
    }

    "return EtmpValidationError when 422 is returned with valid payload" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("id")))

      val downstreamPayload = Json.obj(
        "errors" -> Json.obj(
          "processingDate" -> now,
          "code" -> "003",
          "text" -> "Request could not be processed"
        )
      ).toString()

      stubGet("/etmp/RESTAdapter/pods/reports/qrops-transfer?pstr=12345678AB&qtNumber=QT123456&versionNumber=001", downstreamPayload, UNPROCESSABLE_ENTITY)

      val result: Either[DownstreamError, DownstreamTransferData] = await(connector.getTransfer("12345678AB", Some("QT123456"), Some("001")))

      result mustBe Left(EtmpValidationError(now.toString, "003", "Request could not be processed"))
    }

    "return HipBadRequest when 500 is returned with valid payload" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("id")))

      val downstreamPayload = Json.obj(
        "origin" -> "HoD",
        "response" -> Json.obj(
          "error" -> Json.obj(
            "code" -> "code",
            "message" -> "There's been an error",
            "logID" -> "logID"
          )
        )
      ).toString()

      stubGet("/etmp/RESTAdapter/pods/reports/qrops-transfer?pstr=12345678AB&qtNumber=QT123456&versionNumber=001", downstreamPayload, INTERNAL_SERVER_ERROR)

      val result: Either[DownstreamError, DownstreamTransferData] = await(connector.getTransfer("12345678AB", Some("QT123456"), Some("001")))

      result mustBe Left(HipBadRequest("HoD", "code", "There's been an error", Some("logID")))
    }

    "return HipOriginFailures when 503 is returned with valid payload" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("id")))

      val downstreamPayload = Json.obj(
        "origin" -> "HoD",
        "response" -> Json.obj(
          "failures" -> Seq(
            Json.obj(
              "type" -> "type",
              "reason" -> "reason"
            )
          )
        )
      ).toString()

      stubGet("/etmp/RESTAdapter/pods/reports/qrops-transfer?pstr=12345678AB&qtNumber=QT123456&versionNumber=001", downstreamPayload, SERVICE_UNAVAILABLE)

      val result: Either[DownstreamError, DownstreamTransferData] = await(connector.getTransfer("12345678AB", Some("QT123456"), Some("001")))

      result mustBe Left(HipOriginFailures("HoD", List(Failure("type", "reason"))))
    }
  }
}
