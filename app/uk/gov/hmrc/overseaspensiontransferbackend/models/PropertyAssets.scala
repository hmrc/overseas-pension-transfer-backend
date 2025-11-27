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
import play.api.libs.json.{__, Json, Reads, Writes}

case class PropertyAssets(
    recordVersion: Option[String],
    propertyAddress: Option[Address],
    propValue: Option[BigDecimal],
    propDescription: Option[String]
  )

object PropertyAssets {
  implicit val reads: Reads[PropertyAssets]   = Json.reads[PropertyAssets]
  implicit val writes: Writes[PropertyAssets] = Json.writes[PropertyAssets]

  val auditWrites: Writes[PropertyAssets] = (
    (__ \ "recordVersion").writeNullable[String] and
      (__ \ "address").writeNullable[Address] and
      (__ \ "value").writeNullable[BigDecimal] and
      (__ \ "description").writeNullable[String]
  )(pa => (pa.recordVersion, pa.propertyAddress, pa.propValue, pa.propDescription))

  val upstreamReads: Reads[PropertyAssets] = (
    (__ \ "recordVersion").readNullable[String] and
      (__ \ "propertyAddress").readNullable[Address](Address.upstreamReads) and
      (__ \ "propValue").readNullable[BigDecimal] and
      (__ \ "propDescription").readNullable[String]
  )(PropertyAssets.apply _)
}
