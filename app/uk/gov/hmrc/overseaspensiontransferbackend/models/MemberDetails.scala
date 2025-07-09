package uk.gov.hmrc.overseaspensiontransferbackend.models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import java.time.LocalDate

case class MemberDetails(
                          foreName: Option[String]                               = None,
                          lastName: Option[String]                               = None,
                          dateOfBirth: Option[LocalDate]                         = None,
                          nino: Option[String]                                   = None,
                          memberNoNino: Option[String]                           = None,
                          principalResAddDetails: Option[PrincipalResAddDetails] = None,
                          memberResidencyDetails: Option[MemberResidencyDetails] = None
                        )

object MemberDetails {

  implicit val reads: Reads[MemberDetails] = (
    (__ \ "foreName").readNullable[String] and
      (__ \ "lastName").readNullable[String] and
      (__ \ "dateOfBirth").readNullable[LocalDate] and
      (__ \ "nino").readNullable[String] and
      (__ \ "memberNoNino").readNullable[String] and
      (__ \ "principalResAddDetails").readNullable[PrincipalResAddDetails] and
      (__ \ "memberResidencyDetails").readNullable[MemberResidencyDetails]
    )(MemberDetails.apply _)

  implicit val writes: OWrites[MemberDetails] = Json.writes[MemberDetails]

  implicit val format: OFormat[MemberDetails] = OFormat(reads, writes)
}
