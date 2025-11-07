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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class TransferIdSpec extends AnyFreeSpec with Matchers {

  "TransferId" - {
    "return QtNumber" - {
      "When Json value is valid QtNumber" in {
        JsString("QT123456").as[TransferId] mustBe QtNumber("QT123456")
      }
    }

    "return TransferNumber" - {
      "When Json value is valid TransferNumber" in {
        JsString("TR-001").as[TransferId] mustBe TransferNumber("TR-001")
      }
    }

    "return JsError" - {
      "When Json value is Invalid" in {
        intercept[JsResultException] {
          JsBoolean(true).as[TransferId] mustBe JsError("Unable to read as valid TransferId")
        }
      }
    }

    "Write as Json when QtNumber" in {
      Json.toJson(QtNumber("QT123456")) mustBe JsString("QT123456")
    }

    "Write as Json when TransferNumber" in {
      Json.toJson(TransferNumber("TR-001")) mustBe JsString("TR-001")
    }
  }

}
