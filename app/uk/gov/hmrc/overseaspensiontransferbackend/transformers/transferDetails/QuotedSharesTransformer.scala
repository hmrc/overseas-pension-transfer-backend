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

import play.api.libs.json.{JsArray, JsError, JsObject, JsPath, Json, Reads}
import uk.gov.hmrc.overseaspensiontransferbackend.models.{QuotedShares, UnquotedShares}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.steps.{moveStep, TransformerStep}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{PathAwareTransformer, TransformerUtils}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.EnumTransformerStep

class QuotedSharesTransformer extends PathAwareTransformer with EnumTransformerStep {

  override def externalPath: JsPath = JsPath \ "transferDetails" \ "quotedShares"

  override def internalPath: JsPath = JsPath \ "transferDetails" \ "typeOfAssets" \ "quotedShares"

  /** Applies a transformation from raw frontend input (e.g. UserAnswersDTO.data) into the correct internal shape for AnswersData.
    */
  override def construct(input: JsObject): Either[JsError, JsObject] = {
    implicit val reads: Reads[QuotedShares] = QuotedShares.upstreamReads

    val enumConversion: List[QuotedShares] => JsArray = listOfShares =>
      JsArray(listOfShares.map(Json.toJson(_)))

    val steps: Seq[TransformerStep] = Seq(
      constructEnum[List[QuotedShares]](externalPath, enumConversion),
      moveStep(externalPath, internalPath)
    )

    TransformerUtils.applyPipeline(input, steps)(identity)
  }

  /** Applies the reverse transformation to make stored data suitable for frontend rendering.
    */
  override def deconstruct(input: JsObject): Either[JsError, JsObject] = {
    val enumConversion: List[QuotedShares] => JsArray = quotedShares =>
      JsArray(quotedShares.map(_.toUpstreamJson))

    val steps: Seq[TransformerStep] = Seq(
      constructEnum[List[QuotedShares]](internalPath, enumConversion),
      moveStep(internalPath, externalPath)
    )

    TransformerUtils.applyPipeline(input, steps)(identity)
  }

}
