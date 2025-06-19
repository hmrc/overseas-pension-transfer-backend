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

import org.scalatest.freespec.AnyFreeSpec
import play.api.Application
import play.api.inject.bind
import play.api.test.Helpers.running
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository

import scala.concurrent.Future

class SaveForLaterServiceSpec extends AnyFreeSpec with SpecBase {

  "SaveForLaterService" - {

    "getAnswers" - {

      "must return Some(...) if repository returns a SavedUserAnswers" in {
        val mockRepository = mock[SaveForLaterRepository]

        when(mockRepository.get(eqTo(testId))).thenReturn(Future.successful(Some(simpleSavedUserAnswers)))

        val app: Application =
          applicationBuilder()
            .overrides(bind[SaveForLaterRepository].toInstance(mockRepository))
            .build()

        running(app) {
          val service = app.injector.instanceOf[SaveForLaterService]
          val result  = service.getAnswers(testId).futureValue

          result mustBe Some(simpleSavedUserAnswers)
          verify(mockRepository).get(eqTo(testId))
        }
      }

      "must return None if repository returns None" in {
        val mockRepository = mock[SaveForLaterRepository]

        when(mockRepository.get(eqTo(testId))).thenReturn(Future.successful(None))

        val app = applicationBuilder()
          .overrides(bind[SaveForLaterRepository].toInstance(mockRepository))
          .build()

        running(app) {
          val service = app.injector.instanceOf[SaveForLaterService]
          val result  = service.getAnswers(testId).futureValue

          result mustBe None
          verify(mockRepository).get(eqTo(testId))
        }
      }
    }

    "saveAnswers" - {

      "must return Some(...) if set returns true" in {
        val mockRepository = mock[SaveForLaterRepository]

        when(mockRepository.set(eqTo(simpleSavedUserAnswers))).thenReturn(Future.successful(true))

        val app = applicationBuilder()
          .overrides(bind[SaveForLaterRepository].toInstance(mockRepository))
          .build()

        running(app) {
          val service = app.injector.instanceOf[SaveForLaterService]
          val result  = service.saveAnswers(simpleUserAnswersDTO).futureValue

          result mustBe Some(simpleSavedUserAnswers.copy(lastUpdated = result.value.lastUpdated))
          verify(mockRepository).set(eqTo(simpleSavedUserAnswers))
        }
      }

      "must return None if set returns false" in {
        val mockRepository = mock[SaveForLaterRepository]

        when(mockRepository.set(eqTo(simpleSavedUserAnswers))).thenReturn(Future.successful(false))

        val app = applicationBuilder()
          .overrides(bind[SaveForLaterRepository].toInstance(mockRepository))
          .build()

        running(app) {
          val service = app.injector.instanceOf[SaveForLaterService]
          val result  = service.saveAnswers(simpleUserAnswersDTO).futureValue

          result mustBe None
          verify(mockRepository).set(eqTo(simpleSavedUserAnswers))
        }
      }
    }
  }
}
