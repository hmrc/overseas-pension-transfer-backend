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

package uk.gov.hmrc.overseaspensiontransferbackend.stubs

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.http.Status._
import uk.gov.hmrc.overseaspensiontransferbackend.models.UserAnswers
import play.api.libs.json.Json

object CompileAndSubmitStub {

  def stubStoreAnswersUrl(id: String) = s"/overseas-pension-transfer-stubs/store-answers/$id"

  def stubGetAnswersSuccess(id: String): Unit = {
    stubFor(
      get(urlEqualTo(stubStoreAnswersUrl(id)))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(
              s"""{
                 |  "id": "$id",
                 |  "data": { "someField": "someValue" },
                 |  "lastUpdated": "2025-04-10T12:00:00Z"
                 |}""".stripMargin
            )
        )
    )
  }

  def stubGetAnswersNotFound(id: String): Unit = {
    stubFor(
      get(urlEqualTo(stubStoreAnswersUrl(id)))
        .willReturn(
          aResponse()
            .withStatus(NOT_FOUND)
            .withBody("""{"message":"not found"}""")
        )
    )
  }

  def stubGetAnswersServerError(id: String): Unit = {
    stubFor(
      get(urlEqualTo(stubStoreAnswersUrl(id)))
        .willReturn(
          aResponse()
            .withStatus(INTERNAL_SERVER_ERROR)
            .withBody("""{"message":"some server error"}""")
        )
    )
  }

  def stubUpsertAnswersSuccess(answers: UserAnswers): Unit = {
    stubFor(
      put(urlEqualTo(stubStoreAnswersUrl(answers.id)))
        .withRequestBody(equalToJson(Json.toJson(answers).toString()))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(
              s"""{
                 |  "id":"${answers.id}",
                 |  "data": { "updated": true },
                 |  "lastUpdated": "2025-04-12T10:00:00Z"
                 |}""".stripMargin
            )
        )
    )
  }

  def stubUpsertAnswersBadRequest(answers: UserAnswers): Unit = {
    stubFor(
      put(urlEqualTo(stubStoreAnswersUrl(answers.id)))
        .withRequestBody(equalToJson(Json.toJson(answers).toString()))
        .willReturn(
          aResponse()
            .withStatus(BAD_REQUEST)
            .withBody("""{"error":"bad request"}""")
        )
    )
  }
}
