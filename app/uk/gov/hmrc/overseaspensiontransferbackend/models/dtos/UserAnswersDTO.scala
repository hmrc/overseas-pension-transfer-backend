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

package uk.gov.hmrc.overseaspensiontransferbackend.models.dtos

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._

import java.time.Instant

final case class UserAnswersDTO(
    referenceId: String,
    data: JsObject,
    lastUpdated: Instant
  )

object UserAnswersDTO {

  implicit val format: OFormat[UserAnswersDTO] = {
    val reads: Reads[UserAnswersDTO] = (
      (__ \ "referenceId").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read[Instant]
    )(UserAnswersDTO.apply _)

    val writes: OWrites[UserAnswersDTO] = (
      (__ \ "referenceId").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write[Instant]
    )(unlift(UserAnswersDTO.unapply))

    OFormat(reads, writes)
  }
}
