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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{QtNumber, TransferId, TransferNumber}

class QtNumberSpec extends AnyFreeSpec with Matchers {

  "QtNumber domain validation" - {

    "accept valid QT numbers" in {
      QtNumber.from("QT123456") mustBe Right(QtNumber.from("QT123456").toOption.get)
    }

    List(
      "QT12345",
      "QT1234567",
      "Q123456T",
      "qT123456",
      "Qt123456",
      "Q123456",
      "T123456",
      "AB123456",
      "QTA23456",
      "",
      "QT12A456"
    ).foreach { s =>
      s"reject invalid: $s" in {
        QtNumber.from(s).isLeft mustBe true
      }
    }
  }

  "QtNumber JSON Reads" - {

    "parse valid JSON" in {
      Json.fromJson[QtNumber](JsString("QT123456")).isSuccess mustBe true
    }

    "reject invalid JSON" in {
      Json.fromJson[QtNumber](JsString("BAD")).isError mustBe true
    }
  }

  "TransferId Reads" - {

    "parse QT as TransferId" in {
      Json.fromJson[TransferId](JsString("QT123456")).get mustBe a[QtNumber]
    }

    "parse non-QT as TransferNumber" in {
      Json.fromJson[TransferId](JsString("ABC123")).get mustBe a[TransferNumber]
    }
  }
}
