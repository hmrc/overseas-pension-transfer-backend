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

package uk.gov.hmrc.overseaspensiontransferbackend.models.internal

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class MemberResidencyDetails(
    memUkResident: Option[String]                            = None,
    memEverUkResident: Option[String]                        = None,
    lastPrincipalAddDetails: Option[LastPrincipalAddDetails] = None
  )

object MemberResidencyDetails {

  implicit val reads: Reads[MemberResidencyDetails] = (
    (__ \ "memUkResident").readNullable[String] and
      (__ \ "memEverUkResident").readNullable[String] and
      (__ \ "lastPrincipalAddDetails").readNullable[LastPrincipalAddDetails]
  )(MemberResidencyDetails.apply _)

  implicit val writes: OWrites[MemberResidencyDetails] =
    Json.writes[MemberResidencyDetails]

  implicit val format: OFormat[MemberResidencyDetails] =
    OFormat(reads, writes)
}
