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

import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models.UserAnswers

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HttpReadsInstances.readEitherOf

trait CompileAndSubmitConnector {
  def getAnswers(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[UpstreamErrorResponse, UserAnswers]]
  def upsertAnswers(answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[UpstreamErrorResponse, UserAnswers]]
}

@Singleton()
class CompileAndSubmitStubConnectorImpl @Inject() (httpClient: HttpClientV2, appConfig: AppConfig)
    extends CompileAndSubmitConnector {

  private def stubUrl(id: String): URL =
    url"${appConfig.stubStoreAnswers}/$id"

  override def getAnswers(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[UpstreamErrorResponse, UserAnswers]] = {
    httpClient
      .get(stubUrl(id))
      .execute[Either[UpstreamErrorResponse, UserAnswers]]
  }

  override def upsertAnswers(answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[UpstreamErrorResponse, UserAnswers]] = {
    httpClient
      .put(stubUrl(answers.id))
      .withBody(Json.toJson(answers))
      .execute[Either[UpstreamErrorResponse, UserAnswers]]
  }

}
