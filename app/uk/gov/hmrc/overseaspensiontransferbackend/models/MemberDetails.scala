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

package uk.gov.hmrc.overseaspensiontransferbackend.models

import play.api.libs.json.{Json, OFormat}
import java.time.LocalDate

case class MemberDetails(
    foreName: Option[String]                               = None,
    lastName: Option[String]                               = None,
    nino: Option[String]                                   = None,
    dateOfBirth: Option[LocalDate]                         = None,
    principalResAddDetails: Option[Address]                = None,
    memberResidencyDetails: Option[MemberResidencyDetails] = None
  )

object MemberDetails {

  implicit val format: OFormat[MemberDetails] = Json.format
}
