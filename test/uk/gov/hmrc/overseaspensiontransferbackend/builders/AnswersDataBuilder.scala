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

package uk.gov.hmrc.overseaspensiontransferbackend.builders

import uk.gov.hmrc.overseaspensiontransferbackend.models.{AnswersData, MemberDetails, QropsDetails, SchemeManagerDetails, TransferDetails}

case class AnswersDataBuilder(
    memberDetails: Option[MemberDetails]               = Some(MemberDetails(Some("Firstname"))),
    qropsDetails: Option[QropsDetails]                 = None,
    schemeManagerDetails: Option[SchemeManagerDetails] = None,
    transferDetails: Option[TransferDetails]           = None
  ) {

  def withMembersDetails(m: MemberDetails): AnswersDataBuilder =
    copy(memberDetails = Some(m))

  def withQropsDetails(q: QropsDetails): AnswersDataBuilder =
    copy(qropsDetails = Some(q))

  def withSchemeManagerDetails(s: SchemeManagerDetails): AnswersDataBuilder =
    copy(schemeManagerDetails = Some(s))

  def withTransferDetails(t: TransferDetails): AnswersDataBuilder =
    copy(transferDetails = Some(t))

  def build(): AnswersData =
    AnswersData(memberDetails, qropsDetails, schemeManagerDetails, transferDetails)
}
