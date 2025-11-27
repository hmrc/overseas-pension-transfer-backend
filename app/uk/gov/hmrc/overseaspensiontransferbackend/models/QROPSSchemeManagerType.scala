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
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

case class QROPSSchemeManagerType(
    schemeManagerType: Option[SchemeManagerType],
    schemeManagerAddress: Option[SchemeManagerAddress],
    schemeManagerEmail: Option[String],
    schemeManagerPhone: Option[String],
    qropsIndividual: Option[QROPSIndividual],
    qropsOrganisation: Option[QROPSOrganisation]
  )

object QROPSSchemeManagerType extends JsonHelpers {

  implicit val reads: Reads[QROPSSchemeManagerType] = (
    (__ \ "schemeManagerType").readNullable[SchemeManagerType] and
      (__ \ "schemeManagerAddress").readNullable[SchemeManagerAddress] and
      (__ \ "schemeManagerEmail").readNullable[String] and
      (__ \ "schemeManagerPhone").readNullable[String] and
      (__ \ "qropsIndividual").readNullable[QROPSIndividual] and
      (__ \ "qropsOrganisation").readNullable[QROPSOrganisation]
  )(QROPSSchemeManagerType.apply _)

  implicit val writes: OWrites[QROPSSchemeManagerType] = Json.writes[QROPSSchemeManagerType]

  val auditWrites: OWrites[QROPSSchemeManagerType] = { qropsSchemeManagerType =>
    optField("type", qropsSchemeManagerType.schemeManagerType) ++
      qropsSchemeManagerType.schemeManagerAddress.map(schemeManagerAddress =>
        Json.obj("address" -> SchemeManagerAddress.auditWrites.writes(schemeManagerAddress))
      ).getOrElse(Json.obj()) ++
      optField("email", qropsSchemeManagerType.schemeManagerEmail) ++
      optField("phoneNumber", qropsSchemeManagerType.schemeManagerPhone) ++
      qropsSchemeManagerType.qropsIndividual.map(qropsIndividual => Json.obj("individual" -> QROPSIndividual.auditWrites.writes(qropsIndividual))).getOrElse(
        Json.obj()
      ) ++
      qropsSchemeManagerType.qropsOrganisation.map(qropsOrganisation =>
        Json.obj("organisation" -> QROPSOrganisation.auditWrites.writes(qropsOrganisation))
      ).getOrElse(Json.obj())

  }

  implicit val format: OFormat[QROPSSchemeManagerType] = OFormat(reads, writes)
}
