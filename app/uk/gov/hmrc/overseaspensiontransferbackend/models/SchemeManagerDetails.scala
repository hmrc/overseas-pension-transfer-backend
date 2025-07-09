package uk.gov.hmrc.overseaspensiontransferbackend.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SchemeManagerDetails(
                                 schemeManagerType: Option[String]
                               )

object SchemeManagerDetails {
  implicit val reads: Reads[SchemeManagerDetails] =
    (__ \ "schemeManagerType").readNullable[String].map(SchemeManagerDetails.apply)

  implicit val writes: OWrites[SchemeManagerDetails] =
    Json.writes[SchemeManagerDetails]

  implicit val format: OFormat[SchemeManagerDetails] =
    OFormat(reads, writes)
}
