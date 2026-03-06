/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.overseaspensiontransferbackend.connectors

import com.google.inject.Inject
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication.{AuthenticatedUser, PsaUser, PspUser}

import scala.concurrent.{ExecutionContext, Future}

class PensionSchemeConnector @Inject() (appConfig: AppConfig, http: HttpClientV2)(implicit ec: ExecutionContext) {

  def checkAssociation(srn: String, user: AuthenticatedUser)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url        = url"${appConfig.pensionSchemeService}/pensions-scheme/is-psa-associated"
    val userHeader = {
      user match {
        case PsaUser(psaId, internalId, pensionSchemeDetails, affinityGroup) => "psaId" -> psaId.value
        case PspUser(pspId, internalId, pensionSchemeDetails, affinityGroup) => "pspId" -> pspId.value
      }
    }

    http.get(url)
      .setHeader(
        "schemeReferenceNumber" -> srn,
        userHeader
      )
      .execute[Boolean]
  }

}
