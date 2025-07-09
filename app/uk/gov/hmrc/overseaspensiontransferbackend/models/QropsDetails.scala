package uk.gov.hmrc.overseaspensiontransferbackend.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class QropsDetails(
                         qropsName: Option[String]
                       )

object QropsDetails {
  implicit val reads: Reads[QropsDetails] =
    (__ \ "qropsName").readNullable[String].map(QropsDetails.apply)

  implicit val writes: OWrites[QropsDetails] =
    Json.writes[QropsDetails]

  implicit val format: OFormat[QropsDetails] =
    OFormat(reads, writes)
}
