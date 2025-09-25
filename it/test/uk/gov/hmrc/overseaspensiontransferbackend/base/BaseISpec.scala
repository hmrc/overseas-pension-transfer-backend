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

import org.scalatest._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json._
import play.api.{Application, Environment, Mode}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.helpers.WireMockHelper
import uk.gov.hmrc.overseaspensiontransferbackend.models.{AnswersData, PstrNumber, SavedUserAnswers}
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository

import java.time.Instant
import java.util.UUID
import scala.concurrent.duration._
import scala.concurrent.{Await, Awaitable}

trait BaseISpec
    extends AnyFreeSpec
    with Matchers
    with OptionValues
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with GuiceOneServerPerSuite
    with WireMockHelper {

  def servicesConfig: Map[String, String] = Map(
    "play.filters.csrf.header.bypassHeaders.Csrf-Token"          -> "nocheck",
    "microservice.services.hip.host"                             -> WireMockHelper.wireMockHost,
    "microservice.services.hip.port"                             -> WireMockHelper.wireMockPort.toString,
    "microservice.services.auth.host"                            -> WireMockHelper.wireMockHost,
    "microservice.services.auth.port"                            -> WireMockHelper.wireMockPort.toString
  )

  protected def moduleOverrides: Seq[GuiceableModule] = Seq.empty

  implicit override lazy val app: Application =
    new GuiceApplicationBuilder()
      .in(Environment.simple(mode = Mode.Test))
      .configure(servicesConfig)
      .overrides(moduleOverrides: _*)
      .build()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def await[T](awaitable: Awaitable[T]): T =
    Await.result(awaitable, 5.seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    startServer()
  }

  override def afterAll(): Unit = {
    stopServer()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    clearMongoData()
  }

  override def afterEach(): Unit = {
    clearMongoData()
    super.afterEach()
  }

  private def clearMongoData(): Unit = {
    val repo = app.injector.instanceOf[SaveForLaterRepository]
    await(repo.collection.drop().toFuture())
  }


  def freshId(): String = UUID.randomUUID().toString
  def frozenNow(): Instant = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS)

  def dtoFrom(id: String, pstr: PstrNumber, js: JsObject, now: Instant): UserAnswersDTO =
    UserAnswersDTO(id, pstr, js, now)

  def savedFrom(id: String, pstr: PstrNumber, js: JsObject, now: Instant): SavedUserAnswers =
    SavedUserAnswers(id, pstr, js.as[AnswersData], now)

  def parseJson(str: String): JsObject = Json.parse(str).as[JsObject]

  def withSavedDto(id: String, pstr: PstrNumber, js: JsObject, now: Instant): UserAnswersDTO =
    UserAnswersDTO(id, pstr, js, now)

  def assertJson(js: JsLookupResult, expected: Map[String, String]): Unit = {
    expected.foreach { case (key, value) =>
      (js \ key).as[String] mustBe value
    }
  }

  def assertAddress(js: JsLookupResult, expected: Map[String, String]): Unit = {
    expected.foreach { case (key, value) =>
      (js \ key).as[String] mustBe value
    }
  }

  def assertCountry(js: JsLookupResult, expectedCode: String, expectedName: String): Unit = {
    (js \ "code").as[String] mustBe expectedCode
    (js \ "name").as[String] mustBe expectedName
  }
}
