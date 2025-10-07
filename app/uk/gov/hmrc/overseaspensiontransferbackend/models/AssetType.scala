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

import play.api.libs.json._

trait AssetType

object AssetType {

  case object Cash extends AssetType {
    override def toString: String = "cashAssets"
  }

  case object Property extends AssetType {
    override def toString: String = "propertyAsset"
  }

  case object UnquotedShares extends AssetType {
    override def toString: String = "unquotedShareAssets"
  }

  case object QuotedShares extends AssetType {
    override def toString: String = "quotedShareAssets"
  }

  case object Other extends AssetType {
    override def toString: String = "otherAsset"
  }

  implicit val reads: Reads[AssetType] =
    Reads {
      case JsString("cashAssets")          => JsSuccess(Cash)
      case JsString("propertyAsset")       => JsSuccess(Property)
      case JsString("unquotedShareAssets") => JsSuccess(UnquotedShares)
      case JsString("quotedShareAssets")   => JsSuccess(QuotedShares)
      case JsString("otherAsset")          => JsSuccess(Other)
      case _                               => JsError("Unable to parse value as AssetType")
    }

  implicit val writes: Writes[AssetType] =
    Writes {
      assetType => JsString(assetType.toString)
    }
}
