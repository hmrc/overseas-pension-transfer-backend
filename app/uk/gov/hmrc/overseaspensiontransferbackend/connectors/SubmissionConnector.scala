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
import play.api.libs.json._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.parsers.ParserHelpers.handleUpstreamResponse
import uk.gov.hmrc.overseaspensiontransferbackend.models.upstream.{UpstreamError, UpstreamSuccess}
import uk.gov.hmrc.overseaspensiontransferbackend.validators.ValidatedSubmission

import java.net.URL
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait SubmissionConnector {
  def submit(validated: ValidatedSubmission)(implicit hc: HeaderCarrier): Future[Either[UpstreamError, UpstreamSuccess]]
}

@Singleton
class SubmissionConnectorImpl @Inject() (
    httpClientV2: HttpClientV2,
    appConfig: AppConfig
  )(implicit ec: ExecutionContext
  ) extends SubmissionConnector {

  override def submit(validated: ValidatedSubmission)(implicit hc: HeaderCarrier): Future[Either[UpstreamError, UpstreamSuccess]] = {

    val url = new URL(s"${appConfig.etmpBaseUrl}/RESTAdapter/pods/reports/qrops-transfer")

    val payload: JsValue = Json.toJson(validated.saved.data)

    // TODO: These headers are a first pass and should be discussed and clarified with HIP

    // Required headers from the spec
    val correlationId = UUID.randomUUID().toString
    val receiptDate   = Instant.now().toString // UTC ISO-8601 per spec

    httpClientV2
      .post(url)
      .setHeader(
        // spec headers
        "correlationid"         -> correlationId,
        "X-Message-Type"        -> "FileQROPSTransfer",
        "X-Originating-System"  -> "MDTP",
        "X-Receipt-Date"        -> receiptDate,
        "X-Regime-Type"         -> "PODS",
        "X-Transmitting-System" -> "HIP"
      )
      .withBody(payload)
      .execute[HttpResponse]
      .map(handleUpstreamResponse)
  }
}
