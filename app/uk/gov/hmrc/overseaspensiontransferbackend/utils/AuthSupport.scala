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

package uk.gov.hmrc.overseaspensiontransferbackend.utils

import play.api.Logging
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication._

trait AuthSupport extends Logging {

  def buildPredicate(config: AppConfig): Predicate = {
    AuthProviders(GovernmentGateway) and
      (Enrolment(config.psaEnrolment.serviceName)
        or Enrolment(config.pspEnrolment.serviceName))
  }

  def extractUser(
      enrolments: Enrolments,
      config: AppConfig,
      internalId: String,
      affinityGroup: AffinityGroup
    ): AuthenticatedUser = {
    val matched = enrolments.enrolments.collectFirst {
      case e if e.key == config.psaEnrolment.serviceName => (e, config.psaEnrolment, Psa)
      case e if e.key == config.pspEnrolment.serviceName => (e, config.pspEnrolment, Psp)
    }.getOrElse(throw new RuntimeException("Unable to retrieve matching PSA or PSP enrolment"))

    val (enrolment, enrolmentConfig, userType) = matched

    val id = enrolment.getIdentifier(enrolmentConfig.identifierKey)
      .map(_.value)
      .getOrElse(throw new RuntimeException(s"Missing identifier for ${enrolment.key}"))

    userType match {
      case Psa => PsaUser(PsaId(id), internalId, affinityGroup = affinityGroup)
      case Psp => PspUser(PspId(id), internalId, affinityGroup = affinityGroup)
    }
  }

  def getOrElseFailWithUnauthorised[T](opt: Option[T], message: String): T = {
    opt.getOrElse {
      logger.warn(message)
      throw new IllegalStateException(message)
    }
  }
}
