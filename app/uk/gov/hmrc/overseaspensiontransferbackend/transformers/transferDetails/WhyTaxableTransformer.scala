/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferDetails

import play.api.libs.json.{JsError, JsObject, JsPath, JsString}
import uk.gov.hmrc.overseaspensiontransferbackend.models.WhyTransferTaxable
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.steps.{moveStep, TransformerStep}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.EnumTransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{PathAwareTransformer, TransformerUtils}

class WhyTaxableTransformer extends PathAwareTransformer with EnumTransformerStep {

  private val jsonKey = "whyTaxableOT"

  override def externalPath: JsPath = JsPath \ "transferDetails" \ jsonKey

  override def internalPath: JsPath = JsPath \ "transferDetails" \ "taxableOverseasTransferDetails" \ jsonKey

  /** Applies a transformation from raw frontend input (e.g. UserAnswersDTO.data) into the correct internal shape for AnswersData.
    */
  override def construct(input: JsObject): Either[JsError, JsObject] = {
    val enumConversion: WhyTransferTaxable => JsString = whyTransferTaxable =>
      JsString(whyTransferTaxable.downstreamValue)

    val steps: Seq[TransformerStep] = Seq(
      moveStep(
        externalPath,
        internalPath
      ),
      constructEnum[WhyTransferTaxable](
        internalPath,
        enumConversion
      )
    )

    TransformerUtils.applyPipeline(input, steps)(identity)
  }

  /** Applies the reverse transformation to make stored data suitable for frontend rendering.
    */
  override def deconstruct(input: JsObject): Either[JsError, JsObject] = {
    val enumConversion: WhyTransferTaxable => JsString = whyTransferTaxable =>
      JsString(whyTransferTaxable.toString)

    val steps: Seq[TransformerStep] = Seq(
      constructEnum[WhyTransferTaxable](internalPath, enumConversion),
      moveStep(
        internalPath,
        externalPath
      )
    )

    TransformerUtils.applyPipeline(input, steps)(identity)
  }
}
