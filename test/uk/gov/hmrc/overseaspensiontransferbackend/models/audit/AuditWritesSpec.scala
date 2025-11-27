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

import java.time.LocalDate

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

  "When writing MemberDetails to JSON" - {

    "valid JSON should be produced" in {
      val address                 = Some(Address(
        Some("addressline1"),
        Some("addressline2"),
        Some("addressline3"),
        Some("addressline4"),
        Some("addressline5"),
        Some("postcode"),
        Some("country")
      ))
      val principalResAddDetails  = PrincipalResAddDetails(address, Some("poBox"))
      val lastPrincipalAddDetails = LastPrincipalAddDetails(address, Some(LocalDate.of(2020, 1, 1)))
      val memberResidencyDetails  = MemberResidencyDetails(Some("No"), Some("Yes"), Some(lastPrincipalAddDetails))

      val model: MemberDetails = MemberDetails(
        Some("forename"),
        Some("lastName"),
        Some(LocalDate.of(2020, 1, 1)),
        None,
        Some("reasonNoNino"),
        Some(principalResAddDetails),
        Some(memberResidencyDetails)
      )

      val result = MemberDetails.auditWrites.writes(model)

      result mustBe Json.obj(
        "foreName"                    -> "forename",
        "lastName"                    -> "lastName",
        "dateOfBirth"                 -> "2020-01-01",
        "reasonMemberDoesNotHaveNino" -> "reasonNoNino",
        "principalResidentialAddress" -> Json.obj(
          "addressLine1" -> "addressline1",
          "addressLine2" -> "addressline2",
          "addressLine3" -> "addressline3",
          "addressLine4" -> "addressline4",
          "addressLine5" -> "addressline5",
          "countryCode"  -> "country",
          "postcode"     -> "postcode",
          "poBoxNumber"  -> "poBox"
        ),
        "residencyDetails"            -> Json.obj(
          "isTheMemberAUKResident"                -> "No",
          "hasTheMemberPreviouslyBeenAUKResident" -> "Yes",
          "previousUKResidencyDetails"            -> Json.obj(
            "addressLine1"        -> "addressline1",
            "addressLine2"        -> "addressline2",
            "addressLine3"        -> "addressline3",
            "addressLine4"        -> "addressline4",
            "addressLine5"        -> "addressline5",
            "countryCode"         -> "country",
            "postcode"            -> "postcode",
            "dateMemberLeftTheUK" -> "2020-01-01"
          )
        )
      )
    }

    "valid JSON should be produced with optional values" in {
      val address                = Some(Address(
        Some("addressline1"),
        Some("addressline2"),
        Some("addressline3"),
        Some("addressline4"),
        Some("addressline5"),
        Some("postcode"),
        Some("country")
      ))
      val principalResAddDetails = PrincipalResAddDetails(address, Some("poBox"))
      val memberResidencyDetails = MemberResidencyDetails(Some("Yes"), None, None)

      val model: MemberDetails = MemberDetails(
        Some("forename"),
        Some("lastName"),
        Some(LocalDate.of(2020, 1, 1)),
        Some("NINO"),
        None,
        Some(principalResAddDetails),
        Some(memberResidencyDetails)
      )

      val result = MemberDetails.auditWrites.writes(model)

      result mustBe Json.obj(
        "foreName"                    -> "forename",
        "lastName"                    -> "lastName",
        "dateOfBirth"                 -> "2020-01-01",
        "nino"                        -> "NINO",
        "principalResidentialAddress" -> Json.obj(
          "addressLine1" -> "addressline1",
          "addressLine2" -> "addressline2",
          "addressLine3" -> "addressline3",
          "addressLine4" -> "addressline4",
          "addressLine5" -> "addressline5",
          "countryCode"  -> "country",
          "postcode"     -> "postcode",
          "poBoxNumber"  -> "poBox"
        ),
        "residencyDetails"            -> Json.obj(
          "isTheMemberAUKResident" -> "Yes"
        )
      )
    }
  }

  "When writing TransferDetails to JSON" - {

    "valid JSON should be produced for taxable overseas transfers with all assets" in {
      val address        = Address(
        Some("line 1"),
        Some("line 2"),
        Some("line 3"),
        Some("line 4"),
        Some("line 5"),
        Some("postcode"),
        Some("country")
      )
      val propertyAsset  = PropertyAssets(Some("001"), Some(address), Some(100), Some("propertyDescription"))
      val unquotedShares = UnquotedShares(Some("001"), Some(100), Some(100), Some("company"), Some("shareClass"))
      val quotedShares   = QuotedShares(Some("001"), Some(100), Some(100), Some("company"), Some("shareClass"))

      val otherAssets                    = OtherAssets(Some("001"), Some(100), Some("assetDescription"))
      val typeOfAssets                   =
        TypeOfAssets(
          Some("001"),
          Some("Yes"),
          Some(100),
          Some("Yes"),
          Some("Yes"),
          Some(List(unquotedShares, unquotedShares, unquotedShares, unquotedShares, unquotedShares)),
          Some("Yes"),
          Some("Yes"),
          Some(List(quotedShares, quotedShares, quotedShares, quotedShares, quotedShares)),
          Some("Yes"),
          Some("Yes"),
          Some(List(propertyAsset, propertyAsset, propertyAsset, propertyAsset, propertyAsset)),
          Some("Yes"),
          Some("Yes"),
          Some(List(otherAssets, otherAssets, otherAssets, otherAssets, otherAssets))
        )
      val taxableOverseasTransferDetails =
        TaxableOverseasTransferDetails(
          Some(TransferExceedsOTCAllowance),
          Some(Seq(ApplicableExclusion("01"))),
          Some(100),
          Some(100)
        )

      val model: TransferDetails = TransferDetails(
        Some("001"),
        Some(100),
        Some(100),
        Some(LocalDate.of(2020, 1, 1)),
        Some("No"),
        Some("Yes"),
        None,
        Some(taxableOverseasTransferDetails),
        Some(typeOfAssets)
      )

      val propertyJson = Json.obj(
        "recordVersion" -> "001",
        "address"       -> Json.obj(
          "addressLine1" -> "line 1",
          "addressLine2" -> "line 2",
          "addressLine3" -> "line 3",
          "addressLine4" -> "line 4",
          "addressLine5" -> "line 5",
          "country"      -> "country",
          "ukPostCode"   -> "postcode"
        ),
        "value"         -> 100,
        "description"   -> "propertyDescription"
      )

      val quotedSharesJson = Json.obj(
        "recordVersion"        -> "001",
        "value"                -> 100,
        "numberOfQuotedShares" -> 100,
        "companyName"          -> "company",
        "quotedSharesClass"    -> "shareClass"
      )

      val unquotedSharesJson = Json.obj(
        "recordVersion"          -> "001",
        "value"                  -> 100,
        "numberOfUnquotedShares" -> 100,
        "companyName"            -> "company",
        "unquotedSharesClass"    -> "shareClass"
      )

      val otherAssetsJson = Json.obj(
        "recordVersion"    -> "001",
        "assetValue"       -> 100,
        "assetDescription" -> "assetDescription"
      )

      val result = TransferDetails.auditWrites.writes(model)

      result mustBe Json.obj(
        "recordVersion"                  -> "001",
        "totalAmount"                    -> 100,
        "totalAllowanceBeforeTransfer"   -> 100,
        "date"                           -> "2020-01-01",
        "isTransferCashOnly"             -> "No",
        "isTheTransferTaxableOverseas"   -> "Yes",
        "taxableOverseasTransferDetails" -> Json.obj(
          "reasonCode"                   -> "01",
          "applicableExclusionsCodes"    -> Json.arr("01"),
          "amountOfTaxDeducted"          -> 100,
          "transferAmountMinusTaxAmount" -> 100
        ),
        "assets"                         -> Json.obj(
          "recordVersion"                   -> "001",
          "transferContainsCash"            -> "Yes",
          "cashValue"                       -> 100,
          "transferContainsUnquotedShares"  -> "Yes",
          "areThereMoreThan5UnquotedShares" -> "Yes",
          "unquotedSharesDetails"           -> Json.arr(
            unquotedSharesJson,
            unquotedSharesJson,
            unquotedSharesJson,
            unquotedSharesJson,
            unquotedSharesJson
          ),
          "transferContainsQuotedShares"    -> "Yes",
          "areThereMoreThan5QuotedShares"   -> "Yes",
          "quotedSharesDetails"             -> Json.arr(
            quotedSharesJson,
            quotedSharesJson,
            quotedSharesJson,
            quotedSharesJson,
            quotedSharesJson
          ),
          "transferContainsPropertyAssets"  -> "Yes",
          "areThereMoreThan5PropertyAssets" -> "Yes",
          "propertyAssetsDetails"           -> Json.arr(
            propertyJson,
            propertyJson,
            propertyJson,
            propertyJson,
            propertyJson
          ),
          "transferContainsOtherAssets"     -> "Yes",
          "areThereMoreThan5OtherAssets"    -> "Yes",
          "otherAssetsDetails"              -> Json.arr(
            otherAssetsJson,
            otherAssetsJson,
            otherAssetsJson,
            otherAssetsJson,
            otherAssetsJson
          )
        )
      )
    }

    "valid JSON should be produced for not taxable overseas transfers and cash only" in {
      val model: TransferDetails = TransferDetails(
        Some("001"),
        Some(100),
        Some(100),
        Some(LocalDate.of(2020, 1, 1)),
        Some("Yes"),
        Some("No"),
        Some(Seq(Occupational)),
        None,
        None
      )

      val result = TransferDetails.auditWrites.writes(model)

      result mustBe Json.obj(
        "recordVersion"                              -> "001",
        "totalAmount"                                -> 100,
        "totalAllowanceBeforeTransfer"               -> 100,
        "date"                                       -> "2020-01-01",
        "isTransferCashOnly"                         -> "Yes",
        "isTheTransferTaxableOverseas"               -> "No",
        "reasonCodesWhyTransferIsNotTaxableOverseas" -> Json.arr("01")
      )
    }
  }
}
