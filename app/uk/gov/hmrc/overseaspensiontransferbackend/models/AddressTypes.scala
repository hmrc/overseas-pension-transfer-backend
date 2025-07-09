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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

import java.time.LocalDate

case class PrincipalResAddDetails(
                                   addressDetails: Option[Address],
                                   poBoxNumber: Option[String]
                                 ) extends AddressBase {
  override def addressLine1: String         = addressDetails.map(_.addressLine1).orNull
  override def addressLine2: String         = addressDetails.map(_.addressLine2).orNull
  override def addressLine3: Option[String] = addressDetails.flatMap(_.addressLine3)
  override def addressLine4: Option[String] = addressDetails.flatMap(_.addressLine4)
  override def addressLine5: Option[String] = addressDetails.flatMap(_.addressLine5)
  override def ukPostCode: Option[String]   = addressDetails.flatMap(_.ukPostCode)
  override def country: Option[Country]     = addressDetails.flatMap(_.country)
}

object PrincipalResAddDetails {
  implicit val reads: Reads[PrincipalResAddDetails] = (
    (__ \ "addressDetails").readNullable[Address] and
      (__ \ "poBoxNumber").readNullable[String]
    )(PrincipalResAddDetails.apply _)

  implicit val writes: OWrites[PrincipalResAddDetails] = Json.writes[PrincipalResAddDetails]
  implicit val format: OFormat[PrincipalResAddDetails] = OFormat(reads, writes)
}


case class LastPrincipalAddDetails(
                                    addressDetails: Option[Address],
                                    dateMemberLeftUk: Option[LocalDate]
                                  ) extends AddressBase {
  override def addressLine1: String         = addressDetails.map(_.addressLine1).orNull
  override def addressLine2: String         = addressDetails.map(_.addressLine2).orNull
  override def addressLine3: Option[String] = addressDetails.flatMap(_.addressLine3)
  override def addressLine4: Option[String] = addressDetails.flatMap(_.addressLine4)
  override def addressLine5: Option[String] = addressDetails.flatMap(_.addressLine5)
  override def ukPostCode: Option[String]   = addressDetails.flatMap(_.ukPostCode)
  override def country: Option[Country]     = addressDetails.flatMap(_.country)
}

object LastPrincipalAddDetails {
  implicit val reads: Reads[LastPrincipalAddDetails] = (
    (__ \ "addressDetails").readNullable[Address] and
      (__ \ "dateMemberLeftUk").readNullable[LocalDate]
    )(LastPrincipalAddDetails.apply _)

  implicit val writes: OWrites[LastPrincipalAddDetails] = Json.writes[LastPrincipalAddDetails]
  implicit val format: OFormat[LastPrincipalAddDetails] = OFormat(reads, writes)
}

case class PropertyAddress(
                            addressLine1: String,
                            addressLine2: String,
                            addressLine3: Option[String],
                            addressLine4: Option[String],
                            addressLine5: Option[String],
                            ukPostCode: Option[String],
                            country: Option[Country]
                          ) extends AddressBase

object PropertyAddress {
  // NB: This may not be validated properly, I'm not sure if the compiler is smart enough to get recognise that
  // it is a form of Address. It might need its own custom reads like the others.
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
  // NB: This may not be validated properly, I'm not sure if the compiler is smart enough to get recognise that
  // it is a form of Address. It might need its own custom reads like the others.
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
  // NB: This may not be validated properly, I'm not sure if the compiler is smart enough to get recognise that
  // it is a form of Address. It might need its own custom reads like the others.
  implicit val format: OFormat[SchemeManagerAddress] = Json.format
}
