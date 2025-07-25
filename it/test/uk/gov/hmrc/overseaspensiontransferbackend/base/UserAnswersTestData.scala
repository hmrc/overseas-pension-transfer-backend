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
        "country"      -> Json.obj(
          "code" -> "AE-AZ",
          "name" -> "Abu Dhabi"
        ),
        "ukPostCode"   -> "AB1 2CD",
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
          "code" -> "UK",
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
          "code" -> "UK",
          "name" -> "United Kingdom"
        )
      ),
      "qropsEstablished"      -> Json.obj(
        "code" -> "UK",
        "name" -> "United Kingdom"
      )
    )
  )
  val schemeManagerDetails: JsObject = Json.obj(
    "schemeManagerDetails" -> Json.obj(
      "schemeManagerType" -> "organisation",
      "schemeManagerAddress" -> Json.obj(
        "addressLine1" -> "42",
        "addressLine2" -> "Sesame Street",
        "ukPostCode" -> "ZZ1 1ZZ",
        "country" -> Json.obj(
          "code" -> "UK",
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

  val transferDetails = Json.obj(
    "transferDetails" -> Json.obj(
      "transferAmount" -> 12345.99,
      "allowanceBeforeTransfer" -> 54321.99,
      "dateMemberTransferred" -> "2012-12-12",
      "cashOnlyTransfer" -> false,
      "paymentTaxableOverseas" -> true,
      "applicableExclusion" -> "occupational",
      "amountTaxDeducted" -> 9876543.21
    )
  )

  val fullUserAnswersExternalJson: JsObject =
    memberDetailsExternalJson.deepMerge(qropsDetailsExternalJson).deepMerge(schemeManagerDetails).deepMerge(transferDetails)

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
            "country"      -> Json.obj(
              "code" -> "AE-AZ",
              "name" -> "Abu Dhabi"
            )
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
              "country"      -> Json.obj(
                "code" -> "UK",
                "name" -> "United Kingdom"
              )
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
        "country"      -> Json.obj(
          "code" -> "UK",
          "name" -> "United Kingdom"
        )
      ),
      "receivingQropsEstablishedDetails" -> Json.obj(
        "qropsEstablished" -> Json.obj(
          "code" -> "UK",
          "name" -> "United Kingdom"
        )
      ),
      "qropsSchemeManagerType" -> Json.obj(
        "schemeManagerType" -> "02",
        "schemeManagerAddress" -> Json.obj(
          "addressLine1" -> "42",
          "addressLine2" -> "Sesame Street",
          "ukPostCode" -> "ZZ1 1ZZ",
          "country" -> Json.obj(
            "code" -> "UK",
            "name" -> "United Kingdom"
          )
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
        "applicableExclusion" -> "01",
        "amountTaxDeducted" -> 9876543.21
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
        "country"      -> Json.obj("code" -> "UK", "name" -> "United Kingdom")
      ),
      "qropsEstablished"      -> Json.obj("code" -> "UK", "name" -> "United Kingdom")
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
        "country"      -> Json.obj("code" -> "UK", "name" -> "United Kingdom")
      ),
      "receivingQropsEstablishedDetails" -> Json.obj(
        "qropsEstablished" -> Json.obj("code" -> "UK", "name" -> "United Kingdom")
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
      "qropsEstablishedOther" -> "Otherland"
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
        "country"      -> Json.obj("code" -> "FR", "name" -> "France")
      ),
      "receivingQropsEstablishedDetails" -> Json.obj(
        "qropsEstablishedOther" -> "Otherland"
      )
    )
  )
}
