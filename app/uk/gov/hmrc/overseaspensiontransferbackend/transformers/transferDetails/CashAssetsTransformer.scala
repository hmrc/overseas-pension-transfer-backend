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

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.models.AssetType.Cash
import uk.gov.hmrc.overseaspensiontransferbackend.models.TypeOfAssets
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.steps.TransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.{BooleanTransformerStep, EnumTransformerStep}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{PathAwareTransformer, TransformerUtils}

class CashAssetsTransformer extends PathAwareTransformer with BooleanTransformerStep with EnumTransformerStep {

  override def externalPath: JsPath = JsPath \ "transferDetails" \ "cashOnlyTransfer"

  override def internalPath: JsPath = JsPath \ "transferDetails" \ "typeOfAssets" \ "cashAssets"

  private val typeOfAssetsPath: JsPath = JsPath \ "transferDetails" \ "typeOfAssets"

  /** Applies a transformation from raw frontend input (e.g. UserAnswersDTO.data) into the correct internal shape for AnswersData.
    */
  override def construct(input: JsObject): Either[JsError, JsObject] = {
    def setCashAssets(): TransformerStep = json => {
      externalPath.asSingleJson(json).validate[String] match {
        case JsSuccess(value, _) if value == "Yes" => setPath(internalPath, JsString(value), json)
        case _                                     => Right(json)
      }
    }
    val steps: Seq[TransformerStep]      = Seq(
      setCashAssets()
    )

    TransformerUtils.applyPipeline(input, steps)(identity)
  }

  /** Applies the reverse transformation to make stored data suitable for frontend rendering.
    */
  override def deconstruct(input: JsObject): Either[JsError, JsObject] = {
    def setCashOnlyTransfer(): TransformerStep = json => {
      typeOfAssetsPath.asSingleJson(json).validate[TypeOfAssets] match {
        case JsSuccess(value, _) =>
          if (value.getAssets.value.length == 1 && value.getAssets.value.contains(JsString(Cash.toString))) {
            setPath(externalPath, JsBoolean(true), json)
          } else {
            setPath(externalPath, JsBoolean(false), json)
          }
        case _                   => Right(input)
      }
    }

    val steps: Seq[TransformerStep] = Seq(
      setCashOnlyTransfer()
    )

    TransformerUtils.applyPipeline(input, steps)(identity)
  }
}
