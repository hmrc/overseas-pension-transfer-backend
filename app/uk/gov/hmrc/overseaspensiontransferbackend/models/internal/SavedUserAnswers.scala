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

package uk.gov.hmrc.overseaspensiontransferbackend.models.internal

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

final case class SavedUserAnswers(
    referenceId: String,
    data: AnswersData,
    lastUpdated: Instant
  )

final case class AnswersData(
    transferringMember: Option[TransferringMember],
    aboutReceivingQROPS: Option[AboutReceivingQROPS],
    schemeManagerDetails: Option[SchemeManagerDetails],
    transferDetails: Option[TransferDetails]
  )

object AnswersData {

  // This may seam unnecessary but allows for type validation in the save for later service.
  // A custom reads with readNullable will need to be written for every key we save to mongo.
  // This is due to the way that play handles nullable values.
  implicit val reads: Reads[AnswersData] = (
    (__ \ AnswersDataField.TransferringMember.toString).readNullable[TransferringMember] and
      (__ \ "aboutReceivingQROPS").readNullable[AboutReceivingQROPS] and
      (__ \ "schemeManagerDetails").readNullable[SchemeManagerDetails] and
      (__ \ "transferDetails").readNullable[TransferDetails]
  )(AnswersData.apply _)

  implicit val writes: OWrites[AnswersData] = Json.writes[AnswersData]

  implicit val format: OFormat[AnswersData] = OFormat(reads, writes)
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
