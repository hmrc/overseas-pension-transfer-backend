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
import uk.gov.hmrc.http.HeaderNames.authorisation
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.parsers.ParserHelpers.handleResponse
import uk.gov.hmrc.overseaspensiontransferbackend.models.PstrNumber
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream.{DownstreamAllTransfersData, DownstreamError, DownstreamSuccess, DownstreamTransferData}
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.QtNumber
import uk.gov.hmrc.overseaspensiontransferbackend.validators.ValidatedSubmission

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate}
import java.util.{Base64, UUID}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[TransferConnectorImpl])
trait TransferConnector {
  def submitTransfer(validated: ValidatedSubmission)(implicit hc: HeaderCarrier): Future[Either[DownstreamError, DownstreamSuccess]]

  def getTransfer(
      pstr: PstrNumber,
      qtNumber: QtNumber,
      versionNumber: String
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
class TransferConnectorImpl @Inject() (
    httpClientV2: HttpClientV2,
    appConfig: AppConfig
  )(implicit ec: ExecutionContext
  ) extends TransferConnector with Logging {

  override def submitTransfer(validated: ValidatedSubmission)(implicit hc: HeaderCarrier): Future[Either[DownstreamError, DownstreamSuccess]] = {

    val url = url"${appConfig.etmpBaseUrl}/etmp/RESTAdapter/pods/reports/qrops-transfer"

    val payload: JsValue = Json.toJson(validated.saved.data)

    // Required headers from the spec
    val correlationId = UUID.randomUUID().toString

    val receiptDate = Instant.now().toString // UTC ISO-8601 per spec

    httpClientV2
      .post(url)
      .setHeader(
        authorisation           -> authorization(),
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
      pstr: PstrNumber,
      qtNumber: QtNumber,
      versionNumber: String
    )(implicit hc: HeaderCarrier
    ): Future[Either[DownstreamError, DownstreamTransferData]] = {

    val url = url"${appConfig.etmpBaseUrl}/etmp/RESTAdapter/pods/reports/qrops-transfer"

    val correlationId     = UUID.randomUUID().toString
    val receiptDate       = Instant.now().toString
    val queryStringParams = Seq("pstr" -> pstr.value, "qtNumber" -> qtNumber.value, "versionNumber" -> versionNumber)

    httpClientV2
      .get(url)
      .setHeader(
        authorisation           -> authorization(),
        "correlationid"         -> correlationId,
        "X-Message-Type"        -> "GetQROPSTransfer",
        "X-Originating-System"  -> "MDTP",
        "X-Receipt-Date"        -> receiptDate,
        "X-Regime-Type"         -> "PODS",
        "X-Transmitting-System" -> "HIP"
      )
      .transform(_.addQueryStringParameters(queryStringParams: _*))
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

    val correlationId = UUID.randomUUID().toString

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
        authorisation           -> authorization(),
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

  private def authorization(): String = {
    val clientId = appConfig.clientId
    val secret   = appConfig.clientSecret

    val encoded = Base64.getEncoder.encodeToString(s"$clientId:$secret".getBytes("UTF-8"))
    s"Basic $encoded"
  }

}
