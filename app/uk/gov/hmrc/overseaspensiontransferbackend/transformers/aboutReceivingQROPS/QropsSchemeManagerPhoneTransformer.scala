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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.aboutReceivingQROPS

import play.api.libs.json.{JsError, JsObject, JsPath}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.steps.{moveStep, TransformerStep}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{PathAwareTransformer, TransformerUtils}

class QropsSchemeManagerPhoneTransformer extends PathAwareTransformer {

  val jsonKey = "schemeManagerPhone"

  override def externalPath: JsPath = JsPath \ "schemeManagerDetails" \ jsonKey

  override def internalPath: JsPath = JsPath \ "aboutReceivingQROPS" \ "qropsSchemeManagerType" \ jsonKey

  /** Applies a transformation from raw frontend input (e.g. UserAnswersDTO.data) into the correct internal shape for AnswersData.
    */
  override def construct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      moveStep(
        from = externalPath,
        to   = internalPath
      )
    )
    TransformerUtils.applyPipeline(json, steps)(identity)
  }

  override def deconstruct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      moveStep(
        from = internalPath,
        to   = externalPath
      )
    )
    TransformerUtils.applyPipeline(json, steps)(identity)
  }
}
