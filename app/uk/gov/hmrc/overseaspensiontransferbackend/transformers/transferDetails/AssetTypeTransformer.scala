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

import play.api.libs.json.*
import uk.gov.hmrc.overseaspensiontransferbackend.models.AssetType.{Cash, Other, Property}
import uk.gov.hmrc.overseaspensiontransferbackend.models.*
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.EnumTransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.Transformer

class AssetTypeTransformer extends Transformer with EnumTransformerStep {

  override val externalPath: JsPath = JsPath \ "transferDetails" \ "typeOfAsset"
  override val internalPath: JsPath = JsPath \ "transferDetails" \ "typeOfAssets"

  override val constructor: Seq[TransformerStep] = {
    val enumConversion: List[AssetType] => JsObject = listOfAssets => {
      val fullAssetTypeSet: Seq[AssetType] =
        Seq(Cash, AssetType.QuotedShares, AssetType.UnquotedShares, Property, Other)

      fullAssetTypeSet.foldLeft(Json.obj()) {
        (acc, asset) =>
          if (listOfAssets.contains(asset)) {
            acc.deepMerge(Json.obj(asset.toString -> "Yes"))
          } else {
            acc.deepMerge(Json.obj(asset.toString -> "No"))
          }
      }
    }
    Seq(constructEnum[List[AssetType]](internalPath, enumConversion))
  }

  override def deconstruct(input: JsObject): Either[JsError, JsObject] = {
    def setTypeOfAsset(): TransformerStep = json => {
      val enumConversion: TypeOfAssets => JsArray = typeOfAssets => {
        typeOfAssets.getAssets
      }

      internalPath.asSingleJson(json).validate[TypeOfAssets] match {
        case JsSuccess(value, _) => setPath(externalPath, enumConversion(value), json)
        case _                   => Right(input)
      }
    }
    def removeKeys(): TransformerStep     = json => {
      val fullAssetTypeSet = Seq(Cash, AssetType.QuotedShares, AssetType.UnquotedShares, Property, Other)

      Right(fullAssetTypeSet.foldLeft(json) {
        (acc, curr) =>
          pruneAtPath(internalPath \ curr.toString)(acc)
      })
    }
    applyPipeline(input, Seq(setTypeOfAsset(), removeKeys()))(identity)
  }
}
