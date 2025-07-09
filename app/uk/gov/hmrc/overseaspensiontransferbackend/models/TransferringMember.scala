package uk.gov.hmrc.overseaspensiontransferbackend.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class TransferringMember(
    memberDetails: Option[MemberDetails] = None
  )

object TransferringMember {

  implicit val reads: Reads[TransferringMember] =
    (__ \ "memberDetails").readNullable[MemberDetails].map(TransferringMember.apply)

  implicit val writes: OWrites[TransferringMember] = Json.writes[TransferringMember]

  implicit val format: OFormat[TransferringMember] = OFormat(reads, writes)
}
