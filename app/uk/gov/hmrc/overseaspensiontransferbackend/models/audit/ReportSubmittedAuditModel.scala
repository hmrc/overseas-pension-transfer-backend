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
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication.{PsaId, PsaPspId, UserType}
import uk.gov.hmrc.overseaspensiontransferbackend.models.{AboutReceivingQROPS, MemberDetails, TransferDetails}
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{QtNumber, TransferId}

case class ReportSubmittedAuditModel(
    referenceId: TransferId,
    journeyType: JourneySubmittedType,
    failureReason: Option[String],
    maybeQTNumber: Option[QtNumber],
    maybeMemberDetails: Option[MemberDetails],
    maybeTransferDetails: Option[TransferDetails],
    maybeAboutReceivingQROPS: Option[AboutReceivingQROPS],
    roleLoggedInAs: UserType,
    affinityGroup: AffinityGroup,
    requesterIdentifier: PsaPspId,
    maybeAuthorisingSchemeAdministratorID: Option[PsaId]
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

  private val authorisingSchemeAdministratorID: JsObject =
    maybeAuthorisingSchemeAdministratorID match {
      case Some(id) => Json.obj("authorisingSchemeAdministratorID" -> id)
      case None     => Json.obj()
    }

  override val detail: JsValue = Json.obj(
    "internalReportReferenceId" -> referenceId,
    "journeyType"               -> journeyType.toString
  ) ++ failureOutcome ++ qtNumber ++ memberDetails ++ transferDetails ++ receivingQROPS ++ Json.obj(
    "roleLoggedInAs"      -> roleLoggedInAs,
    "affinityGroup"       -> affinityGroup,
    "requesterIdentifier" -> requesterIdentifier.toString
  ) ++ authorisingSchemeAdministratorID
}

object ReportSubmittedAuditModel {

  def build(
      referenceId: TransferId,
      journeyType: JourneySubmittedType,
      failureReason: Option[String],
      maybeQTNumber: Option[QtNumber],
      maybeMemberDetails: Option[MemberDetails],
      maybeTransferDetails: Option[TransferDetails],
      maybeAboutReceivingQROPS: Option[AboutReceivingQROPS],
      userInfo: AuditUserInfo
    ): ReportSubmittedAuditModel =
    ReportSubmittedAuditModel(
      referenceId,
      journeyType,
      failureReason,
      maybeQTNumber,
      maybeMemberDetails,
      maybeTransferDetails,
      maybeAboutReceivingQROPS,
      userInfo.roleLoggedInAs,
      userInfo.affinityGroup,
      userInfo.requesterIdentifier,
      userInfo.maybeAuthorisingSchemeAdministratorID
    )
}
