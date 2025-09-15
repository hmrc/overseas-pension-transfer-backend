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

package uk.gov.hmrc.overseaspensiontransferbackend.controllers

import org.scalatest.freespec.AnyFreeSpec
import play.api.Application
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase

class GetAllSubmissionsControllerSpec
    extends AnyFreeSpec
    with SpecBase {

  private val endpoint = "/overseas-pension-transfer-backend/get-all-submissions"

  "GetAllSubmissionsController.getAllSubmissions" - {

    "must return 200 with JSON body \"blah\"" in {
      val app: Application = applicationBuilder().build()

      running(app) {
        val pstr    = "24000001AA"
        val request = FakeRequest(GET, s"$endpoint/$pstr")
        val result  = route(app, request).value

        status(result)        mustBe OK
        contentAsJson(result) mustBe Json.toJson("blah")
      }
    }
  }
}
