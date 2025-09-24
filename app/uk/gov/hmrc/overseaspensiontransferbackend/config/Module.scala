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
import play.api.Configuration
import uk.gov.hmrc.overseaspensiontransferbackend.services._
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{UserAnswersTransformer, UserAnswersTransformerFactory}
import uk.gov.hmrc.overseaspensiontransferbackend.utils.CountryCodeReader
import uk.gov.hmrc.overseaspensiontransferbackend.validators.{DummySubmissionValidatorImpl, SubmissionValidator}

import java.time.Clock

class Module extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[AppConfig]).asEagerSingleton()
    bind(classOf[SaveForLaterService]).to(classOf[SaveForLaterServiceImpl])
    // TODO: These must be bound to the actual version in production
    bind(classOf[SubmissionService]).to(classOf[DummySubmissionServiceImpl])
    bind(classOf[SubmissionValidator]).to(classOf[DummySubmissionValidatorImpl])
    bind(classOf[Clock]).toInstance(Clock.systemUTC()) // explicit UTC Clock
  }

  @Provides
  @Singleton
  def provideUserAnswersTransformer(countryCodeReader: CountryCodeReader): UserAnswersTransformer =
    new UserAnswersTransformerFactory(countryCodeReader).build()

  @Provides
  @Singleton
  def provideEncryptionService(config: Configuration): EncryptionService = {
    val master = config.getOptional[String]("mongodb.localMasterKey")
      .orElse(config.getOptional[String]("encryption.masterKey"))
      .getOrElse(
        throw new IllegalStateException("encryption master key not configured (mongodb.localMasterKey or encryption.masterKey)")
      )
    new EncryptionService(master)
  }
}
