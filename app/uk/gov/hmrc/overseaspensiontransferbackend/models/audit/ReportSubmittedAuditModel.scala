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

import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.overseaspensiontransferbackend.models.{AboutReceivingQROPS, MemberDetails, TransferDetails}
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.QtNumber

case class ReportSubmittedAuditModel(
    referenceId: String,
    journeyType: JourneySubmittedType,
    correlationId: String,
    failureReason: Option[String],
    maybeQTNumber: Option[QtNumber],
    maybeMemberDetails: Option[MemberDetails],
    maybeTransferDetails: Option[TransferDetails],
    maybeAboutReceivingQROPS: Option[AboutReceivingQROPS]
  ) extends JsonAuditModel {
  override val auditType: String = "overseasPensionTransferReportSubmitted"

  private val qtNumber =
    maybeQTNumber match {
      case Some(qt) => Json.obj("overseasPensionTransferReportReference" -> qt.value)
      case None     => Json.obj()
    }

  private val failureOutcome =
    failureReason match {
      case Some(reason) => Json.obj("failureReason" -> reason)
      case None         => Json.obj()
    }

  private val memberDetails: JsObject =
    maybeMemberDetails match {
      case Some(memberDetails) => Json.obj("member" -> memberDetails)
      case None                => Json.obj()
    }

  private val transferDetails: JsObject =
    maybeTransferDetails match {
      case Some(transferDetails) => Json.obj("pensionTransfer" -> transferDetails)
      case None                  => Json.obj()
    }

  private val receivingQROPS: JsObject =
    maybeAboutReceivingQROPS match {
      case Some(qropsDetails) => Json.obj("qualifyingRecognisedOverseasPensionScheme" -> qropsDetails)
      case None               => Json.obj()
    }

//  Both?
//  "roleLoggedInAs": "PSP", // "PSP"
//  "affinityGroup": "Organisation", // "Individual"
//  "requesterIdentifier": "21000002", // "A2000002"
//  "authorisingSchemeAdministratorID": "A1000002"

  override val detail: JsValue = Json.obj(
    "internalReportReferenceId" -> referenceId,
    "journeyType"               -> journeyType.toString,
    "correlationId"             -> correlationId
  ) ++ failureOutcome ++ qtNumber ++ memberDetails ++ transferDetails ++ receivingQROPS
}

object ReportSubmittedAuditModel {

  def build(
      referenceId: String,
      journeyType: JourneySubmittedType,
      correlationId: String,
      failureReason: Option[String],
      maybeQTNumber: Option[QtNumber],
      maybeMemberDetails: Option[MemberDetails],
      maybeTransferDetails: Option[TransferDetails],
      maybeAboutReceivingQROPS: Option[AboutReceivingQROPS]
    ): ReportSubmittedAuditModel =
    ReportSubmittedAuditModel(
      referenceId,
      journeyType,
      correlationId,
      failureReason,
      maybeQTNumber,
      maybeMemberDetails,
      maybeTransferDetails,
      maybeAboutReceivingQROPS
    )
}
