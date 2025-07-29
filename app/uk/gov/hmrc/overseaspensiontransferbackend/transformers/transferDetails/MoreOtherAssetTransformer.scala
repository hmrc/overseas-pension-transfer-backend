package uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferDetails

import play.api.libs.json.{JsError, JsObject, JsPath}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{PathAwareTransformer, TransformerUtils}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.steps.{TransformerStep, moveStep}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.BooleanTransformerStep

class MoreOtherAssetTransformer extends PathAwareTransformer with BooleanTransformerStep {

  override def externalPath: JsPath = JsPath \ "transferDetails" \ "moreAsset"

  override def internalPath: JsPath = JsPath \ "transferDetails" \ "typeOfAssets" \ "moreAsset"

  /** Applies a transformation from raw frontend input (e.g. UserAnswersDTO.data) into the correct internal shape for AnswersData.
   */
  override def construct(input: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      constructBool(externalPath),
      moveStep(externalPath, internalPath)
    )

    TransformerUtils.applyPipeline(input, steps)(identity)
  }

  /** Applies the reverse transformation to make stored data suitable for frontend rendering.
   */
  override def deconstruct(input: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      deconstructBool(internalPath),
      moveStep(internalPath, externalPath)
    )

    TransformerUtils.applyPipeline(input, steps)(identity)
  }
}
