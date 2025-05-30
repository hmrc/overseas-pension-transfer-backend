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

/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.overseaspensiontransferbackend.helpers

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status.OK

object WireMockHelper {
  val wireMockHost                        = "localhost"
  val wireMockPort                        = 11111
  val wireMockConf: WireMockConfiguration = new WireMockConfiguration().port(wireMockPort)
  val wireMockServer                      = new WireMockServer(wireMockConf)
}

trait WireMockHelper {

  self: GuiceOneServerPerSuite =>

  import WireMockHelper._

  def startServer(): Unit = {
    wireMockServer.start()
    WireMock.configureFor(wireMockPort)
  }

  def stopServer(): Unit = wireMockServer.stop()

  def stubGet(url: String, response: String, status: Int = OK): StubMapping = {
    stubFor(get(url)
      .willReturn(
        aResponse()
          .withBody(response)
          .withStatus(status)
      ))
  }

  def stubPost(url: String, response: String, status: Int = OK): StubMapping = {
    stubFor(post(url)
      .willReturn(
        aResponse()
          .withBody(response)
          .withStatus(status)
      ))
  }

  def stubPut(url: String, response: String, status: Int = OK): StubMapping = {
    stubFor(put(url)
      .willReturn(
        aResponse()
          .withBody(response)
          .withStatus(status)
      ))
  }
}
