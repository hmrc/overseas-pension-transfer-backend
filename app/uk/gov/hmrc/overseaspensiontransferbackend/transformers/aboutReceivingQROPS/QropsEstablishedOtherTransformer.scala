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

import com.google.inject.Inject
import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.steps._
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.EnumTransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{PathAwareTransformer, TransformerUtils}
import uk.gov.hmrc.overseaspensiontransferbackend.utils.{CountryCodeReader, JsonHelpers}

class QropsEstablishedOtherTransformer @Inject() (countryCodeReader: CountryCodeReader) extends PathAwareTransformer with EnumTransformerStep with JsonHelpers {

  val jsonKey = "qropsEstablishedOther"

  override def externalPath: JsPath = JsPath \ "qropsDetails" \ jsonKey

  override def internalPath: JsPath = JsPath \ "aboutReceivingQROPS" \ "receivingQropsEstablishedDetails" \ jsonKey

  private val qropsEstablishedPath = JsPath \ "aboutReceivingQROPS" \ "receivingQropsEstablishedDetails" \ "qropsEstablished"

  /** Applies a transformation from raw frontend input (e.g. UserAnswersDTO.data) into the correct internal shape for AnswersData.
    *
    * If the free-text "other" field is being set by the frontend, this removes any conflicting structured country value.
    */
  override def construct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      conditionalPruneStep(
        onlyIfSetAt = externalPath,
        pruneTarget = qropsEstablishedPath
      ),
      moveStep(
        from        = externalPath,
        to          = internalPath
      )
    )
    TransformerUtils.applyPipeline(json, steps)(identity)
  }

  /** Applies the reverse transformation to make stored data suitable for frontend rendering.
    */
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
