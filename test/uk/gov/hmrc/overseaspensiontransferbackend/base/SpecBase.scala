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

package uk.gov.hmrc.overseaspensiontransferbackend.base

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models.SavedUserAnswers
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO.fromSavedUserAnswers

import java.time.Instant
import scala.concurrent.ExecutionContext

trait SpecBase
    extends Matchers
    with ArgumentMatchersSugar
    with TryValues
    with OptionValues
    with EitherValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar {

  implicit lazy val hc: HeaderCarrier    = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  val mockHttpClient: HttpClientV2       = mock[HttpClientV2]
  val mockAppConfig: AppConfig           = mock[AppConfig]

  val testId: String = "test-id"

  val now: Instant = Instant.parse("2025-04-11T12:00:00Z")

  val incomingJson: JsObject = Json.obj(
    "referenceId" -> "ignored-in-request-body",
    "data"        -> Json.obj("someField" -> "someIncomingValue"),
    "lastUpdated" -> now
  )

  val simpleSavedUserAnswers: SavedUserAnswers = SavedUserAnswers(
    referenceId = testId,
    data        = Json.obj("field" -> "value"),
    lastUpdated = Instant.parse("2025-04-11T12:00:00Z")
  )

  val simpleUserAnswersDTO: UserAnswersDTO = fromSavedUserAnswers(simpleSavedUserAnswers)

  protected def applicationBuilder(userAnswers: Option[SavedUserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
}
