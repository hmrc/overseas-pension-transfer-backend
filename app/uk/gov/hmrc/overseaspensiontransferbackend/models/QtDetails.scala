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
import play.api.libs.json.{__, Reads}
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.QtNumber

import java.time.{Instant, LocalDate}

case class QtDetails(
    qtVersion: String,
    qtStatus: QtStatus,
    receiptDate: Instant,
    qtReference: QtNumber,
    qtTransferDate: Option[LocalDate],
    qtDigitalStatus: Option[String]
  )

object QtDetails {

  implicit val reads: Reads[QtDetails] = (
    (__ \ "qtVersion").read[String] and
      (__ \ "qtStatus").read[String].map(QtStatus.apply) and
      (__ \ "receiptDate").read[Instant] and
      (__ \ "qtReference").read[String].map(QtNumber.apply) and
      (__ \ "qtTransferDate").readNullable[LocalDate] and
      (__ \ "qtDigitalStatus").readNullable[String]
  )(QtDetails.apply _)
}
