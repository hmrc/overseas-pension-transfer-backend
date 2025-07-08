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

import play.api.libs.json.{Json, OFormat}

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
  implicit val format: OFormat[Address] = Json.format
}

case class Country(code: String, name: String) {
  override def toString: String = name
}

object Country {
  implicit val format: OFormat[Country] = Json.format
}
