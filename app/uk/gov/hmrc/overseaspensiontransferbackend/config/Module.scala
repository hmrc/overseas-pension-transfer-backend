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
import uk.gov.hmrc.overseaspensiontransferbackend.services.{SaveForLaterService, SaveForLaterServiceImpl}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{UserAnswersTransformer, UserAnswersTransformerFactory}

import java.time.{Clock, ZoneOffset}

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[AppConfig]).asEagerSingleton()
    bind(classOf[SaveForLaterService]).to(classOf[SaveForLaterServiceImpl])
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC))
  }

  @Provides
  @Singleton
  def provideUserAnswersTransformer(): UserAnswersTransformer =
    UserAnswersTransformerFactory().build()
}
