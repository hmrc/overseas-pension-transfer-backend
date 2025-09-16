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
import play.api.libs.json._

class PstrNumberSpec extends AnyFreeSpec with Matchers {

  "PstrNumber.isValid" - {
    "must return true for a canonical PSTR" in {
      PstrNumber("12345678AB").isValid mustBe true
    }

    "must return true for lowercase letters" in {
      PstrNumber("12345678ab").isValid mustBe true
    }

    "must return true when surrounded by whitespace (validated on compacted form)" in {
      PstrNumber("  12345678AB  ").isValid mustBe true
    }

    "must return true when spaces are between digits/letters" in {
      PstrNumber("1234 5678 AB").isValid    mustBe true
      PstrNumber("12 34 56 78 A B").isValid mustBe true
    }

    "must return true when whitespace includes tabs/newlines" in {
      PstrNumber("12\t34\n56 78 AB").isValid mustBe true
    }

    "must return false for wrong length or shape after compacting" in {
      PstrNumber("1234567 AB").isValid   mustBe false // 7 digits + 2 letters
      PstrNumber("12345678 ABC").isValid mustBe false // 8 digits + 3 letters
      PstrNumber("ABCDEFGH 12").isValid  mustBe false // letters then digits
      PstrNumber("1234 5678 A1").isValid mustBe false // last char not a letter
    }
  }

  "PstrNumber.from" - {
    "must return Right preserving ORIGINAL string (with spaces)" in {
      val in = " 12 345 678 AB "
      PstrNumber.from(in) mustBe Right(PstrNumber(in))
    }

    "must accept lowercase and preserve as-is" in {
      PstrNumber.from("1234 5678 ab") mustBe Right(PstrNumber("1234 5678 ab"))
    }

    "must return Left with the expected message when invalid after compacting" in {
      PstrNumber.from("not-a-pstr") mustBe
        Left("PSTR must be 8 digits followed by 2 letters (e.g. 12345678AB).")
    }
  }

  "JSON Reads" - {
    "must validate compacted input but preserve the original string (with spaces)" in {
      JsString(" 12345678AB ").validate[PstrNumber] mustBe JsSuccess(PstrNumber(" 12345678AB "))
    }

    "must accept internal whitespace and preserve as-is" in {
      JsString("12 34 56 78 AB").validate[PstrNumber]   mustBe JsSuccess(PstrNumber("12 34 56 78 AB"))
      JsString("12\t34\n56 78 AB").validate[PstrNumber] mustBe JsSuccess(PstrNumber("12\t34\n56 78 AB"))
    }

    "must fail with pstr.invalid when the compacted string does not match" in {
      val err = JsString("12345678-A B").validate[PstrNumber]
      err.isError mustBe true
      val (_, errs) = err.asInstanceOf[JsError].errors.head
      errs.head.message mustBe "pstr.invalid"
    }
  }

  "JSON Writes" - {
    "must write exactly as stored (no trimming/normalising)" in {
      Json.toJson(PstrNumber(" 12345678AB "))   mustBe JsString(" 12345678AB ")
      Json.toJson(PstrNumber("12 34 56 78 AB")) mustBe JsString("12 34 56 78 AB")
    }
  }

  "JSON round-trip via Format" - {
    "must preserve the original string exactly, including spaces" in {
      val original = PstrNumber("12 34 56 78 AB")
      val json     = Json.toJson(original)
      json                mustBe JsString("12 34 56 78 AB")
      json.as[PstrNumber] mustBe original
    }
  }
}
