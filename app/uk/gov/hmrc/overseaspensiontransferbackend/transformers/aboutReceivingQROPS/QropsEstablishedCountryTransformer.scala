package uk.gov.hmrc.overseaspensiontransferbackend.transformers.aboutReceivingQROPS

import play.api.libs.json.{JsError, JsObject, JsPath, Json, __}
import uk.gov.hmrc.overseaspensiontransferbackend.models.Country
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{PathAwareTransformer, TransformerUtils}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.steps.TransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

class QropsEstablishedCountryTransformer extends PathAwareTransformer with JsonHelpers {

  val jsonKey = "qropsEstablished"

  override def externalPath: JsPath = JsPath \ "qropsDetails" \ jsonKey

  override def internalPath: JsPath = JsPath \ "aboutReceivingQROPS" \ "receivingQropsEstablishedDetails" \ jsonKey

  private val qropsEstablishedOtherPath = JsPath \ "aboutReceivingQROPS" \ "receivingQropsEstablishedDetails" \ "qropsEstablishedOther"

  /** Applies a transformation from raw frontend input (e.g. UserAnswersDTO.data) into the correct internal shape for AnswersData.
   */
  override def construct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      movePath(
        from = externalPath,
        to   = internalPath
      )
    )
    TransformerUtils.applyPipeline(json, steps)(identity)
  }

  /** Applies the reverse transformation to make stored data suitable for frontend rendering.
   */
  override def deconstruct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      movePath(
        from = internalPath,
        to   = externalPath
      )
    )
    TransformerUtils.applyPipeline(json, steps)(identity)
  }
}
