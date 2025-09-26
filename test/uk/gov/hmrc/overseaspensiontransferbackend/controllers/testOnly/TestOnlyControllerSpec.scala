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

package uk.gov.hmrc.overseaspensiontransferbackend.controllers.testOnly

import org.apache.pekko.Done
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.http.Status.NO_CONTENT
import play.api.inject.bind
import play.api.test.Helpers.{defaultAwaitTimeout, status, DELETE}
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository

import scala.concurrent.Future

class TestOnlyControllerSpec extends AnyFreeSpec with Matchers with SpecBase with Injecting {

  private val mockRepository: SaveForLaterRepository = mock[SaveForLaterRepository]

  val app: Application =
    applicationBuilder()
      .overrides(
        bind[SaveForLaterRepository].toInstance(mockRepository)
      )
      .build()

  val controller: TestOnlyController = inject[TestOnlyController]

  "resetDatabase" - {
    "Return 204 No Content" in {
      when(mockRepository.clearAll()).thenReturn(Future.successful(Done))

      val request = FakeRequest(DELETE, "/test-only/reset-test-data")

      val result = controller.resetDatabase(request)

      status(result) mustBe NO_CONTENT
    }
  }
}
