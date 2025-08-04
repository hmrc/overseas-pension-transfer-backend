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

package uk.gov.hmrc.overseaspensiontransferbackend.models

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{__, JsArray, JsString, Json, Reads, Writes}
import uk.gov.hmrc.overseaspensiontransferbackend.models.UnquotedShares.reads

case class TypeOfAssets(
    cashAssets: Option[String],
    cashValue: Option[BigDecimal],
    unquotedShareAssets: Option[String],
    moreUnquoted: Option[String],
    unquotedShares: Option[List[UnquotedShares]],
    quotedShareAssets: Option[String],
    moreQuoted: Option[String],
    quotedShares: Option[List[QuotedShares]],
    propertyAsset: Option[String],
    moreProp: Option[String],
    propertyAssets: Option[List[PropertyAssets]],
    otherAsset: Option[String],
    moreAsset: Option[String],
    otherAssets: Option[List[OtherAssets]]
  ) {

  def getAssets: JsArray = {
    val typeOfAssetsList = List(cashAssets, unquotedShareAssets, quotedShareAssets, propertyAsset, otherAsset)
    val assetTypes       =
      List(AssetType.Cash, AssetType.UnquotedShares, AssetType.QuotedShares, AssetType.Property, AssetType.Other)

    val zipped = assetTypes.zip(typeOfAssetsList)

    zipped.foldLeft(JsArray()) {
      (acc, curr) =>
        curr match {
          case (assetType, Some(value)) if value == "Yes" => acc :+ JsString(assetType.toString)
          case _                                          => acc
        }
    }
  }

}

object TypeOfAssets {

  implicit val reads: Reads[TypeOfAssets] = (
    (__ \ "cashAssets").readNullable[String] and
      (__ \ "cashValue").readNullable[BigDecimal] and
      (__ \ "unquotedShareAssets").readNullable[String] and
      (__ \ "moreUnquoted").readNullable[String] and
      (__ \ "unquotedShares").readNullable[List[UnquotedShares]] and
      (__ \ "quotedShareAssets").readNullable[String] and
      (__ \ "moreQuoted").readNullable[String] and
      (__ \ "quotedShares").readNullable[List[QuotedShares]] and
      (__ \ "propertyAsset").readNullable[String] and
      (__ \ "moreProp").readNullable[String] and
      (__ \ "propertyAssets").readNullable[List[PropertyAssets]] and
      (__ \ "otherAsset").readNullable[String] and
      (__ \ "moreAsset").readNullable[String] and
      (__ \ "otherAssets").readNullable[List[OtherAssets]]
  )(TypeOfAssets.apply _)

  implicit val writes: Writes[TypeOfAssets] = Json.writes[TypeOfAssets]
}
