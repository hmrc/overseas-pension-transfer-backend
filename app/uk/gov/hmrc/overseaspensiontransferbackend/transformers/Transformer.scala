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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers

import play.api.libs.json.{__, JsError, JsObject, JsPath}

trait Transformer extends Steps {
  val jsonKey: String                     = ""
  val externalPath: JsPath                = __
  val internalPath: JsPath                = __
  val constructor: Seq[TransformerStep]   = Seq.empty
  val deconstructor: Seq[TransformerStep] = Seq.empty

  private val moveAsIs: Seq[TransformerStep] =
    if (jsonKey == "") {
      Seq(moveStep(externalPath \ jsonKey, internalPath \ jsonKey))
    } else {
      Seq(moveStep(externalPath, internalPath))
    }

  /** Applies a transformation from raw frontend input (e.g. UserAnswersDTO.data) into the correct internal shape for AnswersData.
    */
  def construct(input: JsObject): Either[JsError, JsObject] = {
    applyPipeline(input, moveAsIs ++ constructor)(identity)
  }

  /** Applies the reverse transformation to make stored data suitable for frontend rendering.
    */
  def deconstruct(input: JsObject): Either[JsError, JsObject] = {
    applyPipeline(input, deconstructor ++ moveAsIs)(identity)
  }

}
