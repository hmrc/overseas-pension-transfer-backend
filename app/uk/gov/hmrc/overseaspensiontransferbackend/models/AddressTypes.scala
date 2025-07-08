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

import java.time.LocalDate

case class PrincipalResAddDetails(
    addressDetails: Address,
    poBoxNumber: Option[String]
  ) extends AddressBase {
  override def addressLine1: String         = addressDetails.addressLine1
  override def addressLine2: String         = addressDetails.addressLine2
  override def addressLine3: Option[String] = addressDetails.addressLine3
  override def addressLine4: Option[String] = addressDetails.addressLine4
  override def addressLine5: Option[String] = addressDetails.addressLine5
  override def ukPostCode: Option[String]   = addressDetails.ukPostCode
  override def country: Option[Country]     = addressDetails.country
}

object PrincipalResAddDetails {
  implicit val format: OFormat[PrincipalResAddDetails] = Json.format
}

case class LastPrincipalAddDetails(
    addressDetails: Address,
    dateMemberLeftUk: Option[LocalDate]
  ) extends AddressBase {

  override def addressLine1: String         = addressDetails.addressLine1
  override def addressLine2: String         = addressDetails.addressLine2
  override def addressLine3: Option[String] = addressDetails.addressLine3
  override def addressLine4: Option[String] = addressDetails.addressLine4
  override def addressLine5: Option[String] = addressDetails.addressLine5
  override def ukPostCode: Option[String]   = addressDetails.ukPostCode
  override def country: Option[Country]     = addressDetails.country
}

object LastPrincipalAddDetails {
  implicit val format: OFormat[LastPrincipalAddDetails] = Json.format
}

case class PropertyAddress(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    addressLine5: Option[String],
    ukPostCode: Option[String],
    country: Option[Country]
  ) extends AddressBase {}

object PropertyAddress {
  implicit val format: OFormat[PropertyAddress] = Json.format
}

case class ReceivingQropsAddress(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    addressLine5: Option[String],
    ukPostCode: Option[String],
    country: Option[Country]
  ) extends AddressBase

object ReceivingQropsAddress {
  implicit val format: OFormat[ReceivingQropsAddress] = Json.format
}

case class SchemeManagerAddress(
    addressLine1: String,
    addressLine2: String,
    addressLine3: Option[String],
    addressLine4: Option[String],
    addressLine5: Option[String],
    ukPostCode: Option[String],
    country: Option[Country]
  ) extends AddressBase

object SchemeManagerAddress {
  implicit val format: OFormat[SchemeManagerAddress] = Json.format
}
