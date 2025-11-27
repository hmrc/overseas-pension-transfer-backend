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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{__, Json, OFormat, OWrites, Reads}
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

case class QROPSOrganisation(
    orgName: Option[String],
    orgForename: Option[String],
    orgSurname: Option[String]
  )

object QROPSOrganisation extends JsonHelpers {

  implicit val reads: Reads[QROPSOrganisation] = (
    (__ \ "orgName").readNullable[String] and
      (__ \ "orgForename").readNullable[String] and
      (__ \ "orgSurname").readNullable[String]
  )(QROPSOrganisation.apply _)

  implicit val writes: OWrites[QROPSOrganisation] = Json.writes[QROPSOrganisation]

  val auditWrites: OWrites[QROPSOrganisation] = { qropsOrganisation =>
    optField("name", qropsOrganisation.orgName) ++
      optField("contactsForename", qropsOrganisation.orgForename) ++
      optField("contactsSurname", qropsOrganisation.orgSurname)
  }

  implicit val format: OFormat[QROPSOrganisation] = OFormat(reads, writes)
}
