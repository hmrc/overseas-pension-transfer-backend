/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.overseaspensiontransferbackend.config

import com.google.inject.{AbstractModule, Provides, Singleton}
import uk.gov.hmrc.overseaspensiontransferbackend.services.{SaveForLaterService, SaveForLaterServiceImpl, SubmissionService, SubmissionServiceImpl}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{UserAnswersTransformer, UserAnswersTransformerFactory}
import uk.gov.hmrc.overseaspensiontransferbackend.utils.CountryCodeReader
import uk.gov.hmrc.overseaspensiontransferbackend.validators.{DummySubmissionValidatorImpl, SubmissionValidator}

import java.time.{Clock, ZoneOffset}

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[AppConfig]).asEagerSingleton()
    bind(classOf[SaveForLaterService]).to(classOf[SaveForLaterServiceImpl])
    // TODO: These must be bound to the actual version in production
    bind(classOf[SubmissionService]).to(classOf[SubmissionServiceImpl])
    bind(classOf[SubmissionValidator]).to(classOf[DummySubmissionValidatorImpl])
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
  }

  @Provides
  @Singleton
  def provideUserAnswersTransformer(countryCodeReader: CountryCodeReader): UserAnswersTransformer =
    new UserAnswersTransformerFactory(countryCodeReader).build()
}
