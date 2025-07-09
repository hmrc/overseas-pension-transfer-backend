package uk.gov.hmrc.overseaspensiontransferbackend.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

case class MemberResidencyDetails(
                                   memUkResident: Option[String]                            = None,
                                   memEverUkResident: Option[String]                        = None,
                                   lastPrincipalAddDetails: Option[LastPrincipalAddDetails] = None
                                 )

object MemberResidencyDetails {

  implicit val reads: Reads[MemberResidencyDetails] = (
    (__ \ "memUkResident").readNullable[String] and
      (__ \ "memEverUkResident").readNullable[String] and
      (__ \ "lastPrincipalAddDetails").readNullable[LastPrincipalAddDetails]
    )(MemberResidencyDetails.apply _)

  implicit val writes: OWrites[MemberResidencyDetails] =
    Json.writes[MemberResidencyDetails]

  implicit val format: OFormat[MemberResidencyDetails] =
    OFormat(reads, writes)
}
