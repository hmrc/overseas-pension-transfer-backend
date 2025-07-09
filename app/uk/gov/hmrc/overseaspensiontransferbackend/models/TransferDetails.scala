package uk.gov.hmrc.overseaspensiontransferbackend.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class TransferDetails(
                            transferAmount: Option[BigDecimal]
                          )

object TransferDetails {
  implicit val reads: Reads[TransferDetails] =
    (__ \ "transferAmount").readNullable[BigDecimal].map(TransferDetails.apply)

  implicit val writes: OWrites[TransferDetails] =
    Json.writes[TransferDetails]

  implicit val format: OFormat[TransferDetails] =
    OFormat(reads, writes)
}
