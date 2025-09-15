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

package uk.gov.hmrc.overseaspensiontransferbackend.models.submission

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.overseaspensiontransferbackend.models.{PstrNumber, QtStatus}

import java.time.LocalDate

case class SubmissionGetAllResponse(
    submissions: Seq[SubmissionGetAllItem]
  )

object SubmissionGetAllResponse {
  implicit val format: OFormat[SubmissionGetAllResponse] = Json.format
}

case class SubmissionGetAllItem(
    transferReference: Option[String],
    qtReference: Option[QtNumber],
    nino: Option[String],
    memberFirstName: Option[String],
    memberSurname: Option[String],
    submissionDate: Option[LocalDate],
    qtStatus: Option[QtStatus],
    schemeId: Option[PstrNumber]
  )

object SubmissionGetAllItem {
  implicit val format: OFormat[SubmissionGetAllItem] = Json.format
}
