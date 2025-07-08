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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Environment, Mode}
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.overseaspensiontransferbackend.helpers.WireMockHelper

import scala.concurrent.duration._
import scala.concurrent.{Await, Awaitable}

trait BaseISpec
    extends AnyWordSpec
    with Matchers
    with OptionValues
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with GuiceOneServerPerSuite
    with WireMockHelper {

  def servicesConfig: Map[String, String] = Map(
    "play.filters.csrf.header.bypassHeaders.Csrf-Token"          -> "nocheck",
    "microservice.services.overseas-pension-transfer-stubs.host" -> WireMockHelper.wireMockHost,
    "microservice.services.overseas-pension-transfer-stubs.port" -> WireMockHelper.wireMockPort.toString,
    "microservice.services.auth.host"                            -> WireMockHelper.wireMockHost,
    "microservice.services.auth.port"                            -> WireMockHelper.wireMockPort.toString,
  )

  implicit override lazy val app: Application =
    new GuiceApplicationBuilder()
      .in(Environment.simple(mode = Mode.Test))
      .configure(servicesConfig)
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
  }

  override def afterEach(): Unit = {
    super.afterEach()
  }
}
