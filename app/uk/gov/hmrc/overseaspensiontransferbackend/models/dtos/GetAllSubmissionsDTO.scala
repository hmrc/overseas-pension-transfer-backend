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

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.models.PstrNumber
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.SubmissionGetAllItem

import java.time.{Clock, Instant}

final case class GetAllSubmissionsDTO(
    pstr: PstrNumber,
    lastUpdated: Instant,
    submissions: Seq[SubmissionGetAllItem]
  )

object GetAllSubmissionsDTO {

  def from(
      pstr: PstrNumber,
      items: Seq[SubmissionGetAllItem]
    )(implicit clock: Clock
    ): GetAllSubmissionsDTO = {
    require(items.nonEmpty, "submissions must be non-empty")
    GetAllSubmissionsDTO(
      pstr        = pstr,
      lastUpdated = Instant.now(clock),
      submissions = items
    )
  }

  implicit val format: OFormat[GetAllSubmissionsDTO] = Json.format[GetAllSubmissionsDTO]
}
