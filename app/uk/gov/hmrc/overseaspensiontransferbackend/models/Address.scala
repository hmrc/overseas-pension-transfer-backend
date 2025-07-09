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

import play.api.libs.functional.syntax._
import play.api.libs.json._

trait AddressBase {
  def addressLine1: String
  def addressLine2: String
  def addressLine3: Option[String]
  def addressLine4: Option[String]
  def addressLine5: Option[String]
  def ukPostCode: Option[String]
  def country: Option[Country]
}

case class Address(
                    addressLine1: String,
                    addressLine2: String,
                    addressLine3: Option[String],
                    addressLine4: Option[String],
                    addressLine5: Option[String],
                    ukPostCode: Option[String],
                    country: Option[Country]
                  )

object Address {
  implicit val reads: Reads[Address] = (
    (__ \ "addressLine1").read[String] and
      (__ \ "addressLine2").read[String] and
      (__ \ "addressLine3").readNullable[String] and
      (__ \ "addressLine4").readNullable[String] and
      (__ \ "addressLine5").readNullable[String] and
      (__ \ "ukPostCode").readNullable[String] and
      (__ \ "country").readNullable[Country]
    )(Address.apply _)

  implicit val writes: OWrites[Address] = Json.writes[Address]

  implicit val format: OFormat[Address] = OFormat(reads, writes)
}

case class Country(code: String, name: String) {
  override def toString: String = name
}

object Country {
  implicit val reads: Reads[Country] = (
    (__ \ "code").read[String] and
      (__ \ "name").read[String]
    )(Country.apply _)

  implicit val writes: OWrites[Country] = Json.writes[Country]

  implicit val format: OFormat[Country] = OFormat(reads, writes)
}
