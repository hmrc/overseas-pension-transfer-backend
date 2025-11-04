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

package uk.gov.hmrc.overseaspensiontransferbackend.models.authentication

import play.api.libs.json.{Format, Json}

final case class PsaId(value: String) extends Submitter {
  override val userType: UserType = Psa
}

object PsaId {
  implicit val format: Format[PsaId]  = Json.format[PsaId]
  val downstreamFormat: Format[PsaId] = Json.valueFormat
}

final case class PspId(value: String) extends Submitter {
  override val userType: UserType = Psp
}

object PspId {
  implicit val format: Format[PspId] = Json.format[PspId]
}
