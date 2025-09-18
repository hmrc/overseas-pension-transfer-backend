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
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.AllTransfersItem

import java.time.{Clock, Instant}

final case class GetAllTransfersDTO(
    pstr: PstrNumber,
    lastUpdated: Instant,
    transfers: Seq[AllTransfersItem]
  )

object GetAllTransfersDTO {

  def from(
      pstr: PstrNumber,
      items: Seq[AllTransfersItem]
    )(implicit clock: Clock
    ): GetAllTransfersDTO = {
    require(items.nonEmpty, "transfers must be non-empty")
    GetAllTransfersDTO(
      pstr        = pstr,
      lastUpdated = Instant.now(clock),
      transfers   = items
    )
  }

  implicit val format: OFormat[GetAllTransfersDTO] = Json.format[GetAllTransfersDTO]
}
