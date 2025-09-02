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

package uk.gov.hmrc.overseaspensiontransferbackend.base

import play.api.libs.json._

object UserAnswersTestData {

  val memberDetailsExternalJson: JsObject = Json.obj(
    "memberDetails" -> Json.obj(
      "name"                    -> Json.obj(
        "firstName" -> "Test",
        "lastName"  -> "McTest"
      ),
      "dateOfBirth"             -> "2011-06-05",
      "nino"                    -> "AB123456B",
      "principalResAddDetails"  -> Json.obj(
        "addressLine1" -> "1",
        "addressLine2" -> "Test road",
        "addressLine3" -> "Testville",
        "addressLine4" -> "East Testerly",
        "ukPostCode"   -> "AB1 2CD",
        "country"      -> Json.obj(
          "code" -> "AE",
          "name" -> "United Arab Emirates"
        ),
        "poBoxNumber"  -> "PO321"
      ),
      "memUkResident"           -> false,
      "memEverUkResident"       -> true,
      "lastPrincipalAddDetails" -> Json.obj(
        "addressLine1" -> "Flat 2",
        "addressLine2" -> "7 Other Place",
        "addressLine3" -> "Some District",
        "ukPostCode"   -> "ZZ1 1ZZ",
        "country"      -> Json.obj(
          "code" -> "GB",
          "name" -> "United Kingdom"
        )
      ),
      "dateMemberLeftUk"        -> "2011-06-06"
    )
  )

  val qropsDetailsExternalJson: JsObject = Json.obj(
    "qropsDetails" -> Json.obj(
      "qropsFullName"         -> "Test Scheme",
      "qropsRef"              -> "AB123",
      "receivingQropsAddress" -> Json.obj(
        "addressLine1" -> "2",
        "addressLine2" -> "QROPS Place",
        "addressLine3" -> "QROPS District",
        "ukPostCode"   -> "ZZ1 1ZZ",
        "country"      -> Json.obj(
          "code" -> "GB",
          "name" -> "United Kingdom"
        )
      ),
      "qropsEstablished"      -> Json.obj(
        "code" -> "GB",
        "name" -> "United Kingdom"
      )
    )
  )
  val schemeManagerDetailsExternalJson: JsObject = Json.obj(
    "schemeManagerDetails" -> Json.obj(
      "schemeManagerType" -> "organisation",
      "schemeManagerAddress" -> Json.obj(
        "addressLine1" -> "42",
        "addressLine2" -> "Sesame Street",
        "ukPostCode" -> "ZZ1 1ZZ",
        "country" -> Json.obj(
          "code" -> "GB",
          "name" -> "United Kingdom"
        )
      ),
      "schemeManagerEmail" -> "scheme.manager@email.com",
      "schemeManagerPhone" -> "07777777777",
      "individualContactName" -> Json.obj(
        "firstName" -> "Individual",
        "lastName" -> "Lastname"
      )
    )
  )

  val transferDetailsExternalJson: JsObject = Json.obj(
    "transferDetails" -> Json.obj(
      "transferAmount" -> 12345.99,
      "allowanceBeforeTransfer" -> 54321.99,
      "dateMemberTransferred" -> "2012-12-12",
      "cashOnlyTransfer" -> false,
      "paymentTaxableOverseas" -> true,
      "applicableExclusion" -> Seq("occupational", "resident"),
      "amountTaxDeducted" -> 9876543.21,
      "transferMinusTax" -> 123456.99,
      "typeOfAsset" -> Seq("cash", "unquotedShares", "other"),
      "moreQuoted" -> false,
      "moreUnquoted" -> true,
      "moreProp" -> false,
      "moreAsset" -> false,
      "quotedShares" -> Seq(
        Json.obj(
          "valueOfShares" -> 1234.99,
          "numberOfShares" -> 54,
          "companyName" -> "Some Company",
          "classOfShares" -> "ABC"
        )
      ),
      "unquotedShares" -> Seq(
        Json.obj(
          "valueOfShares" -> 99999.99,
          "numberOfShares" -> 12,
          "companyName" -> "Unquoted",
          "classOfShares" -> "Class"
        )
      ),
      "propertyAssets" -> Seq(
        Json.obj(
          "propertyAddress" -> Json.obj(
            "addressLine1" -> "11 Test Street",
            "addressLine2" -> "Test Town",
            "ukPostCode" -> "ZZ00 0ZZ",
            "country" -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            )
          ),
          "propValue" -> 650000.00,
          "propDescription" -> "Allotment in London"
        )
      ),
      "otherAssets" -> Seq(
        Json.obj(
          "assetValue" -> 9876.99,
          "assetDescription" -> "Vintage Car"
        )
      )
    )
  )

  val fullUserAnswersExternalJson: JsObject =
    transferDetailsExternalJson.deepMerge(memberDetailsExternalJson).deepMerge(qropsDetailsExternalJson).deepMerge(schemeManagerDetailsExternalJson)

  val transferringMemberInternalJson: JsObject = Json.obj(
    "transferringMember" -> Json.obj(
      "memberDetails" -> Json.obj(
        "foreName"               -> "Test",
        "lastName"               -> "McTest",
        "dateOfBirth"            -> "2011-06-05",
        "nino"                   -> "AB123456B",
        "principalResAddDetails" -> Json.obj(
          "addressDetails" -> Json.obj(
            "addressLine1" -> "1",
            "addressLine2" -> "Test road",
            "addressLine3" -> "Testville",
            "addressLine4" -> "East Testerly",
            "ukPostCode"   -> "AB1 2CD",
            "country"      -> "AE"
          ),
          "poBoxNumber"    -> "PO321"
        ),
        "memberResidencyDetails" -> Json.obj(
          "memUkResident"           -> "No",
          "memEverUkResident"       -> "Yes",
          "lastPrincipalAddDetails" -> Json.obj(
            "addressDetails"   -> Json.obj(
              "addressLine1" -> "Flat 2",
              "addressLine2" -> "7 Other Place",
              "addressLine3" -> "Some District",
              "ukPostCode"   -> "ZZ1 1ZZ",
              "country"      -> "GB"
            ),
            "dateMemberLeftUk" -> "2011-06-06"
          )
        )
      )
    )
  )

  val qropsDetailsInternalJson: JsObject = Json.obj(
    "aboutReceivingQROPS" -> Json.obj(
      "qropsFullName"                    -> "Test Scheme",
      "qropsRef"                         -> "AB123",
      "receivingQropsAddress"            -> Json.obj(
        "addressLine1" -> "2",
        "addressLine2" -> "QROPS Place",
        "addressLine3" -> "QROPS District",
        "ukPostCode"   -> "ZZ1 1ZZ",
        "country"      ->  "GB"
      ),
      "receivingQropsEstablishedDetails" -> Json.obj(
        "qropsEstablished" -> "GB"
      ),
      "qropsSchemeManagerType" -> Json.obj(
        "schemeManagerType" -> "02",
        "schemeManagerAddress" -> Json.obj(
          "addressLine1" -> "42",
          "addressLine2" -> "Sesame Street",
          "ukPostCode" -> "ZZ1 1ZZ",
          "country" -> "GB"
        ),
        "schemeManagerEmail" -> "scheme.manager@email.com",
        "schemeManagerPhone" -> "07777777777",
        "qropsIndividual" -> Json.obj(
          "individualForename" -> "Individual",
          "individualSurname" -> "Lastname"
        )
      )
    )
  )

  val transferDetailsInternalJson: JsObject = Json.obj(
    "transferDetails" -> Json.obj(
      "transferAmount" -> 12345.99,
      "allowanceBeforeTransfer" -> 54321.99,
      "dateMemberTransferred" -> "2012-12-12",
      "cashOnlyTransfer" -> "No",
      "paymentTaxableOverseas" -> "Yes",
      "taxableOverseasTransferDetails" -> Json.obj(
        "applicableExclusion" -> Seq("01", "04"),
        "amountTaxDeducted" -> 9876543.21,
        "transferMinusTax" -> 123456.99
      ),
      "typeOfAssets" -> Json.obj(
        "cashAssets" -> "Yes",
        "quotedShareAssets" -> "No",
        "unquotedShareAssets" -> "Yes",
        "propertyAsset" -> "No",
        "otherAsset" -> "Yes",
        "moreQuoted" -> "No",
        "moreUnquoted" -> "Yes",
        "moreAsset" -> "No",
        "moreProp" -> "No",
        "quotedShares" -> Seq(
          Json.obj(
            "quotedValue" -> 1234.99,
            "quotedShareTotal" -> 54,
            "quotedCompany" -> "Some Company",
            "quotedClass" -> "ABC"
          )
        ),
        "unquotedShares" -> Seq(
          Json.obj(
            "unquotedValue" -> 99999.99,
            "unquotedShareTotal" -> 12,
            "unquotedCompany" -> "Unquoted",
            "unquotedClass" -> "Class"
          )
        ),
        "propertyAssets" -> Seq(
          Json.obj(
            "propertyAddress" -> Json.obj(
              "addressLine1" -> "11 Test Street",
              "addressLine2" -> "Test Town",
              "ukPostCode" -> "ZZ00 0ZZ",
              "country" -> "GB"
            ),
            "propValue" -> 650000.00,
            "propDescription" -> "Allotment in London"
          )
        ),
        "otherAssets" -> Seq(
          Json.obj(
            "assetValue" -> 9876.99,
            "assetDescription" -> "Vintage Car"
          )
        )
      )
    )
  )

  val fullUserAnswersInternalJson: JsObject =
    transferringMemberInternalJson.deepMerge(qropsDetailsInternalJson).deepMerge(transferDetailsInternalJson)

  val memberDetailsExternalUpdateJson: JsObject = Json.obj(
    "memberDetails" -> Json.obj(
      "name" -> Json.obj(
        "firstName" -> "Updated",
        "lastName"  -> "User"
      )
    )
  )

  val qropsDetailsEstablishedExternalJson: JsObject = Json.obj(
    "qropsDetails" -> Json.obj(
      "qropsFullName"         -> "Structured Scheme",
      "qropsRef"              -> "AB123",
      "receivingQropsAddress" -> Json.obj(
        "addressLine1" -> "10",
        "addressLine2" -> "Some Street",
        "addressLine3" -> "Some City",
        "ukPostCode"   -> "ZZ1 1ZZ",
        "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
      ),
      "qropsEstablished"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
    )
  )

  val qropsDetailsEstablishedInternalJson: JsObject = Json.obj(
    "aboutReceivingQROPS" -> Json.obj(
      "qropsFullName"         -> "Structured Scheme",
      "qropsRef"              -> "AB123",
      "receivingQropsAddress" -> Json.obj(
        "addressLine1" -> "10",
        "addressLine2" -> "Some Street",
        "addressLine3" -> "Some City",
        "ukPostCode"   -> "ZZ1 1ZZ",
        "country"      -> "GB"
      ),
      "receivingQropsEstablishedDetails" -> Json.obj(
        "qropsEstablished" -> "GB"
      )
    )
  )

  val qropsDetailsEstablishedOtherExternalJson: JsObject = Json.obj(
    "qropsDetails" -> Json.obj(
      "qropsFullName"         -> "Text Scheme",
      "qropsRef"              -> "CD456",
      "receivingQropsAddress" -> Json.obj(
        "addressLine1" -> "22",
        "addressLine2" -> "Other Street",
        "addressLine3" -> "Other City",
        "ukPostCode"   -> "YY2 2YY",
        "country"      -> Json.obj("code" -> "FR", "name" -> "France")
      ),
      "qropsEstablishedOther" -> Json.obj("code" -> "BR", "name" -> "Brazil")
    )
  )

  val qropsDetailsEstablishedOtherInternalJson: JsObject = Json.obj(
    "aboutReceivingQROPS" -> Json.obj(
      "qropsFullName"         -> "Text Scheme",
      "qropsRef"              -> "CD456",
      "receivingQropsAddress" -> Json.obj(
        "addressLine1" -> "22",
        "addressLine2" -> "Other Street",
        "addressLine3" -> "Other City",
        "ukPostCode"   -> "YY2 2YY",
        "country"      -> "FR"
      ),
      "receivingQropsEstablishedDetails" -> Json.obj(
        "qropsEstablishedOther" -> "BR"
      )
    )
  )
}
