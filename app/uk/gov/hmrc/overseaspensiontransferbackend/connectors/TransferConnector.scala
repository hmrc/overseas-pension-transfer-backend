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
import play.api.http.Status.CREATED
import play.api.libs.json.*
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HeaderNames.authorisation
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.parsers.ParserHelpers.handleResponse
import uk.gov.hmrc.overseaspensiontransferbackend.models.PstrNumber
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream.{DownstreamAllTransfersData, DownstreamError, DownstreamSuccess, DownstreamTransferData}
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.QtNumber
import uk.gov.hmrc.overseaspensiontransferbackend.utils.DateTimeFormats.localDate
import uk.gov.hmrc.overseaspensiontransferbackend.validators.Submission
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.parsers.HttpClientResponse

import java.time.{Instant, LocalDate}
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TransferConnector @Inject() (
    httpClientV2: HttpClientV2,
    appConfig: AppConfig,
    httpClientResponse: HttpClientResponse
  )(implicit ec: ExecutionContext
  ) extends Logging {

  private def buildHeaders(xMessage: String, correlationId: String = UUID.randomUUID().toString): Seq[(String, String)] =
    Seq(
      authorisation           -> appConfig.authorization,
      "correlationid"         -> correlationId,
      "X-Message-Type"        -> xMessage,
      "X-Originating-System"  -> "MDTP",
      "X-Receipt-Date"        -> Instant.now().toString,
      "X-Regime-Type"         -> "PODS",
      "X-Transmitting-System" -> "HIP"
    )

  private val baseUrl = url"${appConfig.etmpBaseUrl}/etmp/RESTAdapter/pods/reports"

  def submitTransfer(validated: Submission, correlationId: String)(implicit hc: HeaderCarrier): Future[Either[DownstreamError, DownstreamSuccess]] = {
    httpClientV2
      .post(url"$baseUrl/qrops-transfer")
      .setHeader(buildHeaders("FileQROPSTransfer", correlationId)*)
      .withBody(Json.toJson(validated))
      .execute[HttpResponse]
      .map(resp => handleResponse[DownstreamSuccess](resp, CREATED))
  }

  def getTransfer(
      pstr: String,
      qtNumber: String,
      versionNumber: String
    )(implicit hc: HeaderCarrier
    ): Future[Either[UpstreamErrorResponse, DownstreamTransferData]] = {
    val queryStringParams = Seq("pstr" -> pstr, "qtNumber" -> qtNumber, "versionNumber" -> versionNumber)
    httpClientResponse.read(
      httpClientV2
        .get(url"$baseUrl/qrops-transfer")
        .setHeader(buildHeaders("GetQROPSTransfer")*)
        .transform(_.addQueryStringParameters(queryStringParams*))
        .execute[Either[UpstreamErrorResponse, DownstreamTransferData]]
    )
  }

  def getAllTransfers(
      pstrNumber: PstrNumber,
      fromDate: LocalDate,
      toDate: LocalDate,
      qtRef: Option[QtNumber]
    )(implicit hc: HeaderCarrier
    ): Future[Either[DownstreamError, DownstreamAllTransfersData]] = {
    val params =
      Seq(
        "dateFrom" -> fromDate.format(localDate),
        "dateTo"   -> toDate.format(localDate),
        "pstr"     -> pstrNumber.normalised
      ) ++ qtRef.map(qt => "qtRef" -> qt.value)
    httpClientV2
      .get(url"$baseUrl/qrops-transfer-overview")
      .transform(_.addQueryStringParameters(params*))
      .setHeader(buildHeaders("GetQTReportOverview")*)
      .execute[HttpResponse]
      .map(resp => handleResponse[DownstreamAllTransfersData](resp))
  }

}
