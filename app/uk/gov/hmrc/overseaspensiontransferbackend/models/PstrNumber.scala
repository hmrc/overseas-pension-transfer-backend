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

import play.api.libs.json._

import scala.util.matching.Regex

case class PstrNumber(value: String) {
  lazy val normalised: String = PstrNumber.normalise(value)
  def isValid: Boolean        = PstrNumber.regex.matches(normalised)
}

object PstrNumber {

  val regex: Regex = "^[0-9]{8}[A-Z]{2}$".r

  def normalise(s: String): String =
    s.replaceAll("\\s+", "").toUpperCase

  def from(s: String): Either[String, PstrNumber] =
    if (regex.matches(normalise(s))) {
      Right(PstrNumber(s))
    } else {
      Left("PSTR must be 8 digits followed by 2 letters (e.g. 12345678AB).")
    }

  /** Validates using normalised input, but keeps the original string exactly as provided. */
  implicit val reads: Reads[PstrNumber] =
    __.read[String]
      .filter(JsonValidationError("pstr.invalid"))(s => regex.matches(normalise(s)))
      .map(PstrNumber(_))

  /** Writes the value exactly as is (no normalise). */
  implicit val writes: Writes[PstrNumber] =
    Writes[PstrNumber](p => JsString(p.value))

  implicit val format: Format[PstrNumber] = Format(reads, writes)
}
