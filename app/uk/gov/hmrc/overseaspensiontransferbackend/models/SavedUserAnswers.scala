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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.overseaspensiontransferbackend.services.EncryptionService

import java.time.Instant

final case class SavedUserAnswers(
    referenceId: String,
    data: AnswersData,
    lastUpdated: Instant
  )

final case class AnswersData(
    reportDetails: Option[ReportDetails],
    transferringMember: Option[TransferringMember],
    aboutReceivingQROPS: Option[AboutReceivingQROPS],
    transferDetails: Option[TransferDetails]
  )

object AnswersData {

  // This may seam unnecessary but allows for type validation in the save for later service.
  // A custom reads with readNullable will need to be written for every key we save to mongo.
  // This is due to the way that play handles nullable values.
  implicit val reads: Reads[AnswersData] = (
    (__ \ "reportDetails").readNullable[ReportDetails] and
      (__ \ "transferringMember").readNullable[TransferringMember] and
      (__ \ "aboutReceivingQROPS").readNullable[AboutReceivingQROPS] and
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

sealed trait AnswersDataWrapper

final case class EncryptedAnswersData(encryptedString: String) extends AnswersDataWrapper {

  def decrypt(implicit encryptionService: EncryptionService): Either[Throwable, DecryptedAnswersData] =
    encryptionService.decrypt(encryptedString).map(json => DecryptedAnswersData(Json.parse(json).as[AnswersData]))
}

final case class DecryptedAnswersData(data: AnswersData) extends AnswersDataWrapper {

  def encrypt(implicit encryptionService: EncryptionService): EncryptedAnswersData =
    EncryptedAnswersData(encryptionService.encrypt(Json.toJson(data).toString()))
}

object AnswersDataWrapper {
  implicit val encryptedFormat: OFormat[EncryptedAnswersData] = Json.format[EncryptedAnswersData]
  implicit val decryptedFormat: OFormat[DecryptedAnswersData] = Json.format[DecryptedAnswersData]

  implicit val wrapperFormat: OFormat[AnswersDataWrapper] = new OFormat[AnswersDataWrapper] {

    override def reads(json: JsValue): JsResult[AnswersDataWrapper] =
      (json \ "encryptedString").validate[String].map(EncryptedAnswersData.apply)
        .orElse((json \ "data").validate[AnswersData].map(DecryptedAnswersData.apply))

    override def writes(o: AnswersDataWrapper): JsObject = o match {
      case e: EncryptedAnswersData => Json.obj("encryptedString" -> e.encryptedString)
      case d: DecryptedAnswersData => Json.obj("data" -> Json.toJson(d.data))
    }
  }
}
