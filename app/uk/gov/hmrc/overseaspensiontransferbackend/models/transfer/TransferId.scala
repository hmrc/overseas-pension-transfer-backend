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

package uk.gov.hmrc.overseaspensiontransferbackend.models.transfer

import play.api.libs.json.*
import play.api.mvc.PathBindable

import scala.util.{Success, Try}

trait TransferId {
  def value: String
}

object TransferId {

  implicit val reads: Reads[TransferId] = Reads {
    json =>
      Try(json.validate[QtNumber]) match {
        case Success(JsSuccess(value, _)) =>
          JsSuccess(value)
        case tn                           =>
          println("\n\n" + tn.get + "\n\n")
          json.validate[TransferNumber] match {
            case JsSuccess(value, _) => JsSuccess(value)
            case _                   => JsError("Unable to read as valid TransferId")
          }
      }
  }

  implicit val writes: Writes[TransferId] = Writes {
    transferId =>
      JsString(transferId.value)
  }

  implicit val format: Format[TransferId] = Format(reads, writes)

  implicit val queryBindable: PathBindable[TransferId] = new PathBindable[TransferId] {

    override def bind(key: String, value: String): Either[String, TransferId] =
      if (value.trim.startsWith("QT")) {
        Right(QtNumber(value))
      } else {
        Right(TransferNumber(value))
      }

    override def unbind(key: String, p: TransferId): String = p.value
  }
}

final case class TransferNumber(value: String) extends TransferId

object TransferNumber {

  implicit val reads: Reads[TransferNumber] =
    (JsPath \ "transferNumber").read[String].map(TransferNumber(_))

  implicit val writes: Writes[TransferNumber] =
    new Writes[TransferNumber] {

      override def writes(o: TransferNumber): JsValue =
        Json.obj("transferNumber" -> o.value)
    }

  implicit val format: Format[TransferNumber] = Format(reads, writes)
}

final case class QtNumber(value: String) extends TransferId {
  require(value.matches("QT[0-9]{6}"))
}

object QtNumber {
  implicit val format: Format[QtNumber] = Json.format[QtNumber]
}
