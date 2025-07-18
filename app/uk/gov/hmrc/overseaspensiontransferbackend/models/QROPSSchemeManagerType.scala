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
import play.api.libs.json._

case class QROPSSchemeManagerType(
    schemeManagerType: Option[String],
    schemeManagerAddress: Option[Address],
    schemeManagerEmail: Option[String],
    schemeManagerPhone: Option[String],
    qropsIndividual: Option[QROPSIndividual],
    qropsOrganisation: Option[QROPSOrganisation]
  )

object QROPSSchemeManagerType {

  implicit val reads: Reads[QROPSSchemeManagerType] = (
    (__ \ "qropsFullName").readNullable[String] and
      (__ \ "schemaManagerAddress").readNullable[Address] and
      (__ \ "schemeManagerEmail").readNullable[String] and
      (__ \ "schemeManagerPhone").readNullable[String] and
      (__ \ "qropsIndividual").readNullable[QROPSIndividual] and
      (__ \ "qropsOrganisation").readNullable[QROPSOrganisation]
  )(QROPSSchemeManagerType.apply _)

  implicit val writes: OWrites[QROPSSchemeManagerType] = Json.writes[QROPSSchemeManagerType]

  implicit val format: OFormat[QROPSSchemeManagerType] = OFormat(reads, writes)
}
