package uk.gov.hmrc.overseaspensiontransferbackend.models.submission

trait TransferRetrievalError

case class TransferNotFound(msg: String) extends TransferRetrievalError
case class TransferDeconstructionError(msg: String) extends TransferRetrievalError

