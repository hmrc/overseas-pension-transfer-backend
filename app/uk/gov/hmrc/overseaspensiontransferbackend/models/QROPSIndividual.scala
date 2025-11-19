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

case class QROPSIndividual(
    individualForename: Option[String],
    individualSurname: Option[String]
  )

object QROPSIndividual extends JsonHelpers {

  implicit val reads: Reads[QROPSIndividual] = (
    (__ \ "individualForename").readNullable[String] and
      (__ \ "individualSurname").readNullable[String]
  )(QROPSIndividual.apply _)

  implicit val writes: OWrites[QROPSIndividual] =
    Json.writes[QROPSIndividual]

  val auditWrites: OWrites[QROPSIndividual] = { qropsIndividual =>
    optField("forename", qropsIndividual.individualForename) ++
      optField("surname", qropsIndividual.individualSurname)
  }

  implicit val format: OFormat[QROPSIndividual] = OFormat(reads, writes)
}
