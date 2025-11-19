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

package uk.gov.hmrc.overseaspensiontransferbackend.models.audit

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.overseaspensiontransferbackend.models._

class AuditWritesSpec extends AnyFreeSpec with Matchers {

  private val receivingQropsAddress = Some(ReceivingQropsAddress(
    Some("addressline1"),
    Some("addressline2"),
    Some("addressline3"),
    Some("addressline4"),
    Some("addressline5"),
    Some("postcode"),
    Some("country")
  ))

  private val receivingQropsEstablishedDetails =
    Some(ReceivingQropsEstablishedDetails(Some("qropsEstablished"), Some("qropsEstablishedOther")))

  private val schemeManagerAddress =
    Some(SchemeManagerAddress(
      Some("addressline1"),
      Some("addressline2"),
      Some("addressline3"),
      Some("addressline4"),
      Some("addressline5"),
      Some("postcode"),
      Some("country")
    ))
  private val schemeManagerEmail   = Some("schemeManagerEmail")
  private val schemeManagerPhone   = Some("schemeManagerPhone")
  private val qropsIndividual      = Some(QROPSIndividual(Some("individualForename"), Some("individualSurname")))

  private val qropsOrganisation =
    Some(QROPSOrganisation(Some("organisationName"), Some("organisationForename"), Some("organisationSurname")))

  "When writing AboutReceivingQROPS to JSON" - {

    "AboutReceivingQROPS and all nested explicit writes" - {

      "must be valid for qrops individual" in {
        val qropsSchemeManagerType = Some(QROPSSchemeManagerType(
          Some(SchemeManagerType("01")),
          schemeManagerAddress,
          schemeManagerEmail,
          schemeManagerPhone,
          qropsIndividual,
          None
        ))

        val model: AboutReceivingQROPS = AboutReceivingQROPS(
          Some("QROPS Fullname"),
          Some("QT123456"),
          receivingQropsAddress,
          receivingQropsEstablishedDetails,
          qropsSchemeManagerType
        )

        val result = AboutReceivingQROPS.auditWrites.writes(model)

        result mustBe Json.obj(
          "fullName"                -> "QROPS Fullname",
          "referenceNumber"         -> "QT123456",
          "address"                 -> Json.obj(
            "addressLine1" -> "addressline1",
            "addressLine2" -> "addressline2",
            "addressLine3" -> "addressline3",
            "addressLine4" -> "addressline4",
            "addressLine5" -> "addressline5",
            "countryCode"  -> "country",
            "postcode"     -> "postcode"
          ),
          "locationOfEstablishment" -> Json.obj(
            "countryCode"           -> "qropsEstablished",
            "otherTerritoryDetails" -> "qropsEstablishedOther"
          ),
          "schemeManagerDetails"    -> Json.obj(
            "type"        -> "01",
            "address"     -> Json.obj(
              "addressLine1" -> "addressline1",
              "addressLine2" -> "addressline2",
              "addressLine3" -> "addressline3",
              "addressLine4" -> "addressline4",
              "addressLine5" -> "addressline5",
              "countryCode"  -> "country",
              "postcode"     -> "postcode"
            ),
            "email"       -> "schemeManagerEmail",
            "phoneNumber" -> "schemeManagerPhone",
            "individual"  -> Json.obj(
              "forename" -> "individualForename",
              "surname"  -> "individualSurname"
            )
          )
        )
      }

      "must be valid for qrops organisation" in {
        val qropsSchemeManagerType = Some(QROPSSchemeManagerType(
          Some(SchemeManagerType("02")),
          schemeManagerAddress,
          schemeManagerEmail,
          schemeManagerPhone,
          None,
          qropsOrganisation
        ))

        val model = AboutReceivingQROPS(
          Some("QROPS Fullname"),
          Some("QT123456"),
          receivingQropsAddress,
          receivingQropsEstablishedDetails,
          qropsSchemeManagerType
        )

        val result = AboutReceivingQROPS.auditWrites.writes(model)

        result mustBe Json.obj(
          "fullName"                -> "QROPS Fullname",
          "referenceNumber"         -> "QT123456",
          "address"                 -> Json.obj(
            "addressLine1" -> "addressline1",
            "addressLine2" -> "addressline2",
            "addressLine3" -> "addressline3",
            "addressLine4" -> "addressline4",
            "addressLine5" -> "addressline5",
            "countryCode"  -> "country",
            "postcode"     -> "postcode"
          ),
          "locationOfEstablishment" -> Json.obj(
            "countryCode"           -> "qropsEstablished",
            "otherTerritoryDetails" -> "qropsEstablishedOther"
          ),
          "schemeManagerDetails"    -> Json.obj(
            "type"         -> "02",
            "address"      -> Json.obj(
              "addressLine1" -> "addressline1",
              "addressLine2" -> "addressline2",
              "addressLine3" -> "addressline3",
              "addressLine4" -> "addressline4",
              "addressLine5" -> "addressline5",
              "countryCode"  -> "country",
              "postcode"     -> "postcode"
            ),
            "email"        -> "schemeManagerEmail",
            "phoneNumber"  -> "schemeManagerPhone",
            "organisation" -> Json.obj(
              "name"             -> "organisationName",
              "contactsForename" -> "organisationForename",
              "contactsSurname"  -> "organisationSurname"
            )
          )
        )
      }
    }
  }
}
