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

import play.api.http.Status._
import play.api.test.Injecting
import uk.gov.hmrc.overseaspensiontransferbackend.base.BaseISpec
import uk.gov.hmrc.overseaspensiontransferbackend.models.UserAnswers
import uk.gov.hmrc.overseaspensiontransferbackend.stubs.CompileAndSubmitStub
import scala.concurrent.ExecutionContext.Implicits.global

import java.time.Instant

class CompileAndSubmitConnectorISpec extends BaseISpec with Injecting {

  private val connector: CompileAndSubmitConnector =
    app.injector.instanceOf[CompileAndSubmitConnector]

  private val testId         = "test-id"

  private val exampleUserAnswers = UserAnswers(
    id          = testId,
    data        = play.api.libs.json.Json.obj("field" -> "value"),
    lastUpdated = Instant.parse("2025-04-10T12:00:00Z")
  )

  "CompileAndSubmitConnector.getAnswers" when {

    "the stubs returns 200 + valid JSON" must {
      "return Right(UserAnswers)" in {
        CompileAndSubmitStub.stubGetAnswersSuccess(testId)

        val result = await(connector.getAnswers(testId))
        result.isRight shouldBe true

        val userAns = result.toOption.value
        userAns.id          shouldBe testId
        userAns.data.\("someField").as[String] shouldBe "someValue"
      }
    }

    "the stubs returns 404" must {
      "return Left(UpstreamErrorResponse) with status 404" in {
        CompileAndSubmitStub.stubGetAnswersNotFound(testId)

        val result = await(connector.getAnswers(testId))
        result.isLeft shouldBe true

        result.left.toOption.value.statusCode shouldBe NOT_FOUND
      }
    }

    "the stubs returns 500" must {
      "return Left(UpstreamErrorResponse) with status 500" in {
        CompileAndSubmitStub.stubGetAnswersServerError(testId)

        val result = await(connector.getAnswers(testId))
        result.isLeft shouldBe true

        result.left.toOption.value.statusCode shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "CompileAndSubmitConnector.upsertAnswers" when {

    "the stubs returns 200 + valid JSON" must {
      "return Right(UserAnswers)" in {
        CompileAndSubmitStub.stubUpsertAnswersSuccess(exampleUserAnswers)

        val result = await(connector.upsertAnswers(exampleUserAnswers))
        result.isRight shouldBe true

        val userAns = result.toOption.value
        userAns.id shouldBe testId
        userAns.data.\("updated").as[Boolean] shouldBe true
      }
    }

    "must return Left(...) if stub returns 400" in {
      CompileAndSubmitStub.stubUpsertAnswersBadRequest(exampleUserAnswers)

      val result = await(connector.upsertAnswers(exampleUserAnswers))
      result.isLeft shouldBe true
      result.left.toOption.value.statusCode shouldBe BAD_REQUEST
    }
  }
}
