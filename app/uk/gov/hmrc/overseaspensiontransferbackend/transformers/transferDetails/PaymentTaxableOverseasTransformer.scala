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

import play.api.libs.json.{JsError, JsObject, JsPath}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.steps.TransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{PathAwareTransformer, TransformerUtils}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.BooleanTransformerStep

class PaymentTaxableOverseasTransformer extends PathAwareTransformer with BooleanTransformerStep {

  override def externalPath: JsPath = JsPath \ "transferDetails" \ "paymentTaxableOverseas"

  override def internalPath: JsPath = externalPath

  /** Applies a transformation from raw frontend input (e.g. UserAnswersDTO.data) into the correct internal shape for AnswersData.
    */
  override def construct(input: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      constructBool(
        internalPath
      )
    )

    TransformerUtils.applyPipeline(input, steps)(identity)
  }

  /** Applies the reverse transformation to make stored data suitable for frontend rendering.
    */
  override def deconstruct(input: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      deconstructBool(
        internalPath
      )
    )

    TransformerUtils.applyPipeline(input, steps)(identity)
  }
}
