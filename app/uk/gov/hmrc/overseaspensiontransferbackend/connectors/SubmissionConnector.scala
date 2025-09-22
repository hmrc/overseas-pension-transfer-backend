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

import com.google.inject.{ImplementedBy, Singleton}
import play.api.Logging
import play.api.http.Status.CREATED
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.parsers.ParserHelpers.handleResponse
import uk.gov.hmrc.overseaspensiontransferbackend.models.PstrNumber
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream.{DownstreamAllTransfersData, DownstreamError, DownstreamSuccess, DownstreamTransferData}
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.QtNumber
import uk.gov.hmrc.overseaspensiontransferbackend.validators.ValidatedSubmission

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[SubmissionConnectorImpl])
trait SubmissionConnector {
  def submit(validated: ValidatedSubmission)(implicit hc: HeaderCarrier): Future[Either[DownstreamError, DownstreamSuccess]]

  def getTransfer(
      pstr: String,
      qtNumber: Option[String],
      versionNumber: Option[String]
    )(implicit hc: HeaderCarrier
    ): Future[Either[DownstreamError, DownstreamTransferData]]

  def getAllTransfers(
      pstrNumber: PstrNumber,
      fromDate: LocalDate,
      toDate: LocalDate,
      qtRef: Option[QtNumber]
    )(implicit hs: HeaderCarrier
    ): Future[Either[DownstreamError, DownstreamAllTransfersData]]
}

@Singleton
class SubmissionConnectorImpl @Inject() (
    httpClientV2: HttpClientV2,
    appConfig: AppConfig
  )(implicit ec: ExecutionContext
  ) extends SubmissionConnector with Logging {

  override def submit(validated: ValidatedSubmission)(implicit hc: HeaderCarrier): Future[Either[DownstreamError, DownstreamSuccess]] = {

    val url = url"${appConfig.etmpBaseUrl}/etmp/RESTAdapter/pods/reports/qrops-transfer"

    val payload: JsValue = Json.toJson(validated.saved.data)

    // Required headers from the spec
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
      .map(resp => handleResponse[DownstreamSuccess](resp, CREATED))
  }

  override def getTransfer(
      pstr: String,
      qtNumber: Option[String],
      versionNumber: Option[String]
    )(implicit hc: HeaderCarrier
    ): Future[Either[DownstreamError, DownstreamTransferData]] = {

    val url = url"${appConfig.etmpBaseUrl}/etmp/RESTAdapter/pods/reports/qrops-transfer?pstr=$pstr&qtNumber=${qtNumber.get}&versionNumber=${versionNumber.get}"

    val correlationId = hc.requestId.fold {
      logger.error("[SubmissionConnector][getTransfer]: Request is missing X-Request-ID header")
      throw new Exception("Header X-Request-ID missing")
    } {
      requestId =>
        requestId.value
    }
    val receiptDate   = Instant.now().toString

    httpClientV2
      .get(url)
      .setHeader(
        "correlationid"         -> correlationId,
        "X-Message-Type"        -> "GetQROPSTransfer",
        "X-Originating-System"  -> "MDTP",
        "X-Receipt-Date"        -> receiptDate,
        "X-Regime-Type"         -> "PODS",
        "X-Transmitting-System" -> "HIP"
      )
      .execute
      .map(resp => handleResponse[DownstreamTransferData](resp))
  }

  override def getAllTransfers(
      pstrNumber: PstrNumber,
      fromDate: LocalDate,
      toDate: LocalDate,
      qtRef: Option[QtNumber]
    )(implicit hc: HeaderCarrier
    ): Future[Either[DownstreamError, DownstreamAllTransfersData]] = {

    val url = url"${appConfig.etmpBaseUrl}/etmp/RESTAdapter/pods/reports/qrops-transfer-overview"

    val correlationId = hc.requestId.fold {
      logger.error("[SubmissionConnector][getAllTransfers]: Request is missing X-Request-ID header")
      throw new Exception("Header X-Request-ID missing")
    }(_.value)

    val receiptDate = Instant.now().toString // UTC ISO-8601 per spec

    val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    val params =
      Seq(
        "dateFrom" -> fromDate.format(formatter),
        "dateTo"   -> toDate.format(formatter),
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
      .map(resp => handleResponse[DownstreamAllTransfersData](resp))
  }
}
