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

import play.api.Configuration
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.services.EncryptionService

object TestAppConfig {

  val masterKey: String = "Test-dGVzdC1rZXktMTIzNDU2Nzg5MDEyMzQ1Ng=="

  def appConfig(): AppConfig = new AppConfig(
    Configuration(
      "appName"                                -> "overseas-pension-transfer-backend-test",
      "microservice.services.auth.host"        -> "localhost",
      "microservice.services.auth.port"        -> 8500,
      "microservice.services.hip.protocol"     -> "http",
      "microservice.services.hip.host"         -> "localhost",
      "microservice.services.hip.port"         -> 15602,
      "microservice.services.hip.clientId"     -> "clientId",
      "microservice.services.hip.clientSecret" -> "clientSecret",
      "mongodb.timeToLiveInDays"               -> 30,
      "mongodb.uri"                            -> "mongodb://localhost:27017/test-saveforlater",
      "mongodb.localMasterKey"                 -> masterKey,
      "getAllTransfers.yearsOffset"            -> 10
    )
  )

  implicit val encryptionService: EncryptionService = new EncryptionService(masterKey)
}
