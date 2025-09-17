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

import com.google.inject.Singleton
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.parsers.ParserHelpers.handleDownstreamResponse
import uk.gov.hmrc.overseaspensiontransferbackend.models.PstrNumber
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream.DownstreamGetAllSuccess.{OverviewItem, Payload}
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream.{DownstreamGetAllError, DownstreamGetAllSuccess, DownstreamSubmittedError, DownstreamSubmittedSuccess}
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.QtNumber
import uk.gov.hmrc.overseaspensiontransferbackend.validators.ValidatedSubmission

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait SubmissionConnector {
  def submit(validated: ValidatedSubmission)(implicit hc: HeaderCarrier): Future[Either[DownstreamSubmittedError, DownstreamSubmittedSuccess]]

  def getAllSubmissions(
      pstrNumber: PstrNumber,
      fromDate: LocalDate,
      toDate: LocalDate,
      qtRef: Option[QtNumber]
    )(implicit hs: HeaderCarrier
    ): Future[Either[DownstreamGetAllError, DownstreamGetAllSuccess]]
}

@Singleton
class SubmissionConnectorImpl @Inject() (
    httpClientV2: HttpClientV2,
    appConfig: AppConfig
  )(implicit ec: ExecutionContext
  ) extends SubmissionConnector with Logging {

  override def submit(validated: ValidatedSubmission)(implicit hc: HeaderCarrier): Future[Either[DownstreamSubmittedError, DownstreamSubmittedSuccess]] = {

    val url = url"${appConfig.etmpBaseUrl}/RESTAdapter/pods/reports/qrops-transfer"

    val payload: JsValue = Json.toJson(validated.saved.data)

    val correlationId = hc.requestId.fold {
      logger.error("[SubmissionConnector][submit]: Request is missing X-Request-ID header")
      throw new Exception("Header X-Request-ID missing")
    } {
      requestId =>
        requestId.value
    }
    val receiptDate   = Instant.now().toString // UTC ISO-8601 per spec

    httpClientV2
      .post(url)
      .setHeader(
        "correlationid"         -> correlationId,
        "X-Message-Type"        -> "FileQROPSTransfer",
        "X-Originating-System"  -> "MDTP",
        "X-Receipt-Date"        -> receiptDate,
        "X-Regime-Type"         -> "PODS",
        "X-Transmitting-System" -> "HIP"
      )
      .withBody(payload)
      .execute[HttpResponse]
      .map(handleDownstreamResponse)
  }

  override def getAllSubmissions(
      pstrNumber: PstrNumber,
      fromDate: LocalDate,
      toDate: LocalDate,
      qtRef: Option[QtNumber]
    )(implicit hc: HeaderCarrier
    ): Future[Either[DownstreamGetAllError, DownstreamGetAllSuccess]] = {

    val url = url"${appConfig.etmpBaseUrl}/RESTAdapter/pods/reports/qrops-transfer-overview"

    val correlationId = hc.requestId.fold {
      logger.error("[SubmissionConnector][getAllSubmissions]: Request is missing X-Request-ID header")
      throw new Exception("Header X-Request-ID missing")
    }(_.value)

    val receiptDate = Instant.now().toString // UTC ISO-8601 per spec

    val formatter = DateTimeFormatter.ofPattern("yyyy MM dd")

    val params =
      Seq(
        "fromDate" -> fromDate.format(formatter),
        "toDate"   -> toDate.format(formatter),
        "pstr"     -> pstrNumber.normalised
      ) ++ qtRef.map(qt => "qtRef" -> qt.value)

    httpClientV2
      .get(url)
      .transform(_.addQueryStringParameters(params: _*))
      .setHeader(
        "correlationid"         -> correlationId,
        "X-Message-Type"        -> "GetQTReportOverview",
        "X-Originating-System"  -> "MDTP",
        "X-Receipt-Date"        -> receiptDate,
        "X-Regime-Type"         -> "PODS",
        "X-Transmitting-System" -> "HIP"
      )
      .execute[HttpResponse]
      .map(handleDownstreamResponse)
  }
}

@Singleton
class DummySubmissionConnectorImpl @Inject() ()(implicit ec: ExecutionContext) extends SubmissionConnector {

  override def submit(validated: ValidatedSubmission)(implicit hc: HeaderCarrier): Future[Either[DownstreamSubmittedError, DownstreamSubmittedSuccess]] = {
    Future.successful(Right(DownstreamSubmittedSuccess(QtNumber("QT123456"), Instant.now(), "formBundleNumber")))
  }

  override def getAllSubmissions(
      pstrNumber: PstrNumber,
      fromDate: LocalDate,
      toDate: LocalDate,
      qtRef: Option[QtNumber]
    )(implicit hs: HeaderCarrier
    ): Future[Either[DownstreamGetAllError, DownstreamGetAllSuccess]] = {

    val payload = DownstreamGetAllSuccess(
      success = Payload(
        qropsTransferOverview = List(
          OverviewItem(
            fbNumber                  = "123456000023",
            qtReference               = "QT564321",
            qtVersion                 = "001",
            qtStatus                  = "Compiled",
            qtDigitalStatus           = "Complied",
            nino                      = "AA000000A",
            firstName                 = "David",
            lastName                  = "Warne",
            qtDate                    = LocalDate.parse("2025-03-14"),
            qropsReference            = "QROPS654321",
            submissionCompilationDate = Instant.parse("2025-05-09T19:10:12Z")
          ),
          OverviewItem(
            fbNumber                  = "123456000024",
            qtReference               = "QT564322",
            qtVersion                 = "003",
            qtStatus                  = "Submitted",
            qtDigitalStatus           = "Submitted",
            nino                      = "AA000001A",
            firstName                 = "Edith",
            lastName                  = "Ennis-Hill",
            qtDate                    = LocalDate.parse("2025-01-01"),
            qropsReference            = "QROPS654322",
            submissionCompilationDate = Instant.parse("2025-05-09T10:10:12Z")
          )
        )
      )
    )
    Future.successful(Right(payload))
  }
}
