package uk.gov.hmrc.overseaspensiontransferbackend.builders

import uk.gov.hmrc.overseaspensiontransferbackend.models.{AnswersData, MemberDetails, QropsDetails, SchemeManagerDetails, TransferDetails}

case class AnswersDataBuilder(
                               memberDetails: Option[MemberDetails] = Some(MemberDetails(Some("Firstname"))),
                               qropsDetails: Option[QropsDetails] = None,
                               schemeManagerDetails: Option[SchemeManagerDetails] = None,
                               transferDetails: Option[TransferDetails] = None
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
