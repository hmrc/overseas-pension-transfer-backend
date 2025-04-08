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

package uk.gov.hmrc.overseaspensiontransferbackend.services

import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReadsInstances.readEitherOf
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models.UserAnswers

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.{Inject, Singleton}

trait CompileAndSubmitService {

  def getAnswers(id: String): Future[Option[UserAnswers]]

  def upsertAnswers(answers: UserAnswers): Future[UserAnswers]
}

@Singleton()
class StubCompileAndSubmitService @Inject() (
                                              httpClient: HttpClientV2,
                                              appConfig: AppConfig
                                            )(implicit ec: ExecutionContext)
  extends CompileAndSubmitService
    with Logging {

  private val stubsBaseUrl: String = appConfig.stubs

  private def stubUrl(id: String): URL =
    url"$stubsBaseUrl/stub-answers/$id"

  /**
   * Retrieve the user's answers from the stubs service.
   * Return `Some(UserAnswers)` if found, or None if the stub returns 404.
   */
  override def getAnswers(id: String): Future[Option[UserAnswers]] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    httpClient
      .get(stubUrl(id))
      .execute[Either[UpstreamErrorResponse, UserAnswers]]
      .map {
        case Right(userAnswers) =>
          Some(userAnswers)
        case Left(error) =>
          if (error.statusCode == 404) None
          else throw error
      }
      .recover {
        case e: Exception =>
          logger.warn(s"Failed to get answers for ID '$id' from stubs: ${e.getMessage}", e)
            None
      }
  }

  /**
   * Upsert the user's answers in the stubs service.
   * Return the updated or created UserAnswers that the stub responds with.
   */
  override def upsertAnswers(answers: UserAnswers): Future[UserAnswers] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val jsonBody: JsValue = Json.toJson(answers)

    httpClient
      .put(stubUrl(answers.id))
      .withBody(jsonBody)
      .execute[UserAnswers]
      .recover {
        case e: Exception =>
          logger.error(s"Failed to upsert answers for ID '${answers.id}' in stubs: ${e.getMessage}", e)
          throw e
      }
  }
}