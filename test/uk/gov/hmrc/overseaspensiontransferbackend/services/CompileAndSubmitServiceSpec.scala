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

import play.api.inject.bind
import play.api.test.Helpers.running
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.CompileAndSubmitConnector
import scala.concurrent.Future
import play.api.Application

class CompileAndSubmitServiceSpec
    extends SpecBase {

  "CompileAndSubmitService" - {

    "getAnswers" - {

      "must return Some(...) if connector returns Right(UserAnswers)" in {
        val mockConnector = mock[CompileAndSubmitConnector]

        when(
          mockConnector.getAnswers(eqTo(testId))(any[HeaderCarrier], any)
        ).thenReturn(Future.successful(Right(simpleUserAnswers)))

        val application: Application =
          applicationBuilder()
            .overrides(
              bind[CompileAndSubmitConnector].toInstance(mockConnector)
            )
            .build()

        running(application) {
          val service = application.injector.instanceOf[CompileAndSubmitService]

          val result = service.getAnswers(testId).futureValue
          result mustBe Some(simpleUserAnswers)

          verify(mockConnector).getAnswers(eqTo(testId))(any[HeaderCarrier], any)
        }
      }

      "must return None if connector returns Left(...) with 404" in {
        val mockConnector = mock[CompileAndSubmitConnector]

        when(
          mockConnector.getAnswers(eqTo(testId))(any[HeaderCarrier], any)
        ).thenReturn(Future.successful(Left(UpstreamErrorResponse("Not Found", 404))))

        val application =
          applicationBuilder()
            .overrides(
              bind[CompileAndSubmitConnector].toInstance(mockConnector)
            )
            .build()

        running(application) {
          val service = application.injector.instanceOf[CompileAndSubmitService]
          val result  = service.getAnswers(testId).futureValue
          result mustBe None
          verify(mockConnector).getAnswers(eqTo(testId))(any[HeaderCarrier], any)
        }
      }

      "must throw if connector returns Left(...) with non-404" in {
        val mockConnector = mock[CompileAndSubmitConnector]

        when(
          mockConnector.getAnswers(eqTo(testId))(any[HeaderCarrier], any)
        ).thenReturn(Future.successful(Left(UpstreamErrorResponse("Server Error", 500))))

        val application =
          applicationBuilder()
            .overrides(
              bind[CompileAndSubmitConnector].toInstance(mockConnector)
            )
            .build()

        running(application) {
          val service = application.injector.instanceOf[CompileAndSubmitService]
          val ex      = service.getAnswers(testId).failed.futureValue

          ex mustBe a[UpstreamErrorResponse]
          ex.asInstanceOf[UpstreamErrorResponse].statusCode mustBe 500
        }
      }
    }

    "upsertAnswers" - {

      "must return the updated UserAnswers on success" in {
        val mockConnector = mock[CompileAndSubmitConnector]

        val updatedAnswers =
          simpleUserAnswers.copy(data = play.api.libs.json.Json.obj("updated" -> true))

        when(
          mockConnector.upsertAnswers(eqTo(simpleUserAnswers))(any[HeaderCarrier], any)
        ).thenReturn(Future.successful(Right(updatedAnswers)))

        val application =
          applicationBuilder()
            .overrides(
              bind[CompileAndSubmitConnector].toInstance(mockConnector)
            )
            .build()

        running(application) {
          val service = application.injector.instanceOf[CompileAndSubmitService]
          val result  = service.upsertAnswers(simpleUserAnswers).futureValue
          result mustBe Some(updatedAnswers)
          verify(mockConnector).upsertAnswers(eqTo(simpleUserAnswers))(any[HeaderCarrier], any)
        }
      }

      "must rethrow an exception if the connector call fails" in {
        val mockConnector = mock[CompileAndSubmitConnector]

        when(
          mockConnector.upsertAnswers(eqTo(simpleUserAnswers))(any[HeaderCarrier], any)
        ).thenReturn(Future.failed(new RuntimeException("Stub error")))

        val application =
          applicationBuilder()
            .overrides(
              bind[CompileAndSubmitConnector].toInstance(mockConnector)
            )
            .build()

        running(application) {
          val service = application.injector.instanceOf[CompileAndSubmitService]
          val ex      = intercept[RuntimeException] {
            service.upsertAnswers(simpleUserAnswers).futureValue
          }
          ex.getMessage must include("Stub error")
        }
      }
    }
  }
}
