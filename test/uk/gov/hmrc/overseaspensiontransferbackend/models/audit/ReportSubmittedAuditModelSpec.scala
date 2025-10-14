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
import play.api.libs.json.Json
import uk.gov.hmrc.overseaspensiontransferbackend.models.{
  AboutReceivingQROPS,
  Address,
  MemberDetails,
  MemberResidencyDetails,
  Occupational,
  PrincipalResAddDetails,
  QROPSIndividual,
  QROPSSchemeManagerType,
  ReceivingQropsAddress,
  ReceivingQropsEstablishedDetails,
  SchemeManagerAddress,
  SchemeManagerType,
  TransferDetails
}
import uk.gov.hmrc.overseaspensiontransferbackend.models.audit.JourneySubmittedType.{SubmissionFailed, SubmissionSucceeded}
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.QtNumber

import java.time.LocalDate

class ReportSubmittedAuditModelSpec extends AnyFreeSpec with Matchers {

  "must create correct minimal json for different journey types" in {
    JourneySubmittedType.values.foreach {
      journey =>
        val result = ReportSubmittedAuditModel.build("internalTransferId", journey, "correlationId", None, None, None, None, None)
        result.auditType   mustBe "overseasPensionTransferReportSubmitted"
        result.journeyType mustBe journey
    }
  }

  "newReportSubmissionSucceeded" - {
    "must build correct json" in {
      val memberDetails = MemberDetails(
        Some("Forename"),
        Some("Last name"),
        Some(LocalDate.of(2000, 1, 1)),
        Some("AA123456A"),
        None,
        Some(PrincipalResAddDetails(
          Some(Address(
            Some("line 1"),
            Some("line 2"),
            Some("line 3"),
            Some("line 4"),
            Some("line 5"),
            Some("ZZ1 1ZZ"),
            Some("UK")
          )),
          None
        )),
        Some(MemberResidencyDetails(Some("true"), None, None))
      )

      val transferDetails = TransferDetails(
        Some(100),
        Some(100),
        Some(LocalDate.of(2013, 1, 1)),
        Some("false"),
        Some("true"),
        Some(Seq(Occupational)),
        None,
        None
      )

      val receivingQROPS = AboutReceivingQROPS(
        Some("Receiving QROPS"),
        None,
        Some(ReceivingQropsAddress(
          Some("line 1"),
          Some("line 2"),
          Some("line 3"),
          Some("line 4"),
          Some("line 5"),
          Some("ZZ1 1ZZ"),
          Some("UK")
        )),
        Some(ReceivingQropsEstablishedDetails(
          Some("Established"),
          Some("Other")
        )),
        Some(QROPSSchemeManagerType(
          Some(SchemeManagerType("01")),
          Some(SchemeManagerAddress(
            Some("line 1"),
            Some("line 2"),
            Some("line 3"),
            Some("line 4"),
            Some("line 5"),
            Some("ZZ1 1ZZ"),
            Some("UK")
          )),
          Some("email.com"),
          Some("111111111111"),
          Some(QROPSIndividual(
            Some("Forename"),
            Some("Surname")
          )),
          None
        ))
      )

      val expectedJson = Json.obj(
        "internalReportReferenceId"                 -> "internalTransferId",
        "journeyType"                               -> "newReportSubmissionSucceeded",
        "correlationId"                             -> "x-request-id",
        "overseasPensionTransferReportReference"    -> "QT123456",
        "member"                                    -> Json.obj(
          "foreName"               -> "Forename",
          "lastName"               -> "Last name",
          "dateOfBirth"            -> "2000-01-01",
          "nino"                   -> "AA123456A",
          "principalResAddDetails" -> Json.obj(
            "addressDetails" -> Json.obj(
              "addressLine1" -> "line 1",
              "addressLine2" -> "line 2",
              "addressLine3" -> "line 3",
              "addressLine4" -> "line 4",
              "addressLine5" -> "line 5",
              "ukPostCode"   -> "ZZ1 1ZZ",
              "country"      -> "UK"
            )
          ),
          "memberResidencyDetails" -> Json.obj(
            "memUkResident" -> "true"
          )
        ),
        "pensionTransfer"                           -> Json.obj(
          "transferAmount"           -> 100,
          "allowanceBeforeTransfer"  -> 100,
          "dateMemberTransferred"    -> "2013-01-01",
          "cashOnlyTransfer"         -> "false",
          "paymentTaxableOverseas"   -> "true",
          "reasonNoOverseasTransfer" -> Json.arr(
            "01"
          )
        ),
        "qualifyingRecognisedOverseasPensionScheme" -> Json.obj(
          "qropsFullName"                    -> "Receiving QROPS",
          "receivingQropsAddress"            -> Json.obj(
            "addressLine1" -> "line 1",
            "addressLine2" -> "line 2",
            "addressLine3" -> "line 3",
            "addressLine4" -> "line 4",
            "addressLine5" -> "line 5",
            "ukPostCode"   -> "ZZ1 1ZZ",
            "country"      -> "UK"
          ),
          "receivingQropsEstablishedDetails" -> Json.obj(
            "qropsEstablished"      -> "Established",
            "qropsEstablishedOther" -> "Other"
          ),
          "qropsSchemeManagerType"           -> Json.obj(
            "schemeManagerType"    -> "01",
            "schemeManagerAddress" -> Json.obj(
              "addressLine1" -> "line 1",
              "addressLine2" -> "line 2",
              "addressLine3" -> "line 3",
              "addressLine4" -> "line 4",
              "addressLine5" -> "line 5",
              "ukPostCode"   -> "ZZ1 1ZZ",
              "country"      -> "UK"
            ),
            "schemeManagerEmail"   -> "email.com",
            "schemeManagerPhone"   -> "111111111111",
            "qropsIndividual"      -> Json.obj(
              "individualForename" -> "Forename",
              "individualSurname"  -> "Surname"
            )
          )
        )
      )

      val result = ReportSubmittedAuditModel.build(
        "internalTransferId",
        SubmissionSucceeded,
        "x-request-id",
        None,
        Some(QtNumber("QT123456")),
        Some(memberDetails),
        Some(transferDetails),
        Some(receivingQROPS)
      )
      result.auditType mustBe "overseasPensionTransferReportSubmitted"
      result.detail    mustBe expectedJson
    }
  }

  "reportSubmissionFailed" - {

    "must create correct json" in {

      val expectedJson = Json.obj(
        "internalReportReferenceId" -> "internalTransferId",
        "journeyType"               -> "reportSubmissionFailed",
        "correlationId"             -> "x-request-id",
        "failureReason"             -> "400 - Bad request"
      )

      val result = ReportSubmittedAuditModel.build(
        "internalTransferId",
        SubmissionFailed,
        "x-request-id",
        Some("400 - Bad request"),
        None,
        None,
        None,
        None
      )
      result.auditType mustBe "overseasPensionTransferReportSubmitted"
      result.detail    mustBe expectedJson
    }
  }
}
