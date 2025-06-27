/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import play.api.libs.functional.syntax._

import java.time.Instant

final case class SavedUserAnswers(
    referenceId: String,
    data: AnswersData,
    lastUpdated: Instant = Instant.now
  )

final case class AnswersData(
    memberDetails: Option[MemberDetails],
    qropsDetails: Option[QropsDetails],
    schemeManagerDetails: Option[SchemeManagerDetails],
    transferDetails: Option[TransferDetails]
  )

object AnswersData {
  implicit val format: OFormat[AnswersData] = Json.format
}

object SavedUserAnswers {

  val reads: Reads[SavedUserAnswers] = {
    (
      (__ \ "referenceId").read[String] and
        (__ \ "data").read[AnswersData] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(SavedUserAnswers.apply _)
  }

  val writes: OWrites[SavedUserAnswers] = {
    (
      (__ \ "referenceId").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(ua => (ua.referenceId, Json.toJsObject(ua.data), ua.lastUpdated))
  }

  implicit val format: OFormat[SavedUserAnswers] = OFormat(reads, writes)
}
