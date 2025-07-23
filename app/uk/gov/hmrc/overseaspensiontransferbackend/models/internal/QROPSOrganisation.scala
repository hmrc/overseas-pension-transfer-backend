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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

case class QROPSOrganisation(
    orgName: Option[String],
    orgTitle: Option[String],
    orgForename: Option[String],
    orgSurname: Option[String]
  )

object QROPSOrganisation {

  implicit val reads: Reads[QROPSOrganisation] = (
    (__ \ "orgName").readNullable[String] and
      (__ \ "orgTitle").readNullable[String] and
      (__ \ "orgForename").readNullable[String] and
      (__ \ "orgSurname").readNullable[String]
  )(QROPSOrganisation.apply _)

  implicit val writes: OWrites[QROPSOrganisation] = Json.writes[QROPSOrganisation]

  implicit val format: OFormat[QROPSOrganisation] = OFormat(reads, writes)
}
