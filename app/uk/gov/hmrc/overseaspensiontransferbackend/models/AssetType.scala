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

trait AssetType {
  val jsonKey: String
}

object AssetType {

  case object Cash extends AssetType {
    override def toString: String = "cash"
    override val jsonKey: String  = "cashAssets"
  }

  case object Property extends AssetType {
    override def toString: String = "property"
    override val jsonKey: String  = "propertyAsset"
  }

  case object UnquotedShares extends AssetType {
    override def toString: String = "unquotedShares"
    override val jsonKey: String  = "unquotedShareAssets"
  }

  case object QuotedShares extends AssetType {
    override def toString: String = "quotedShares"
    override val jsonKey: String  = "quotedShareAssets"
  }

  case object Other extends AssetType {
    override def toString: String = "other"
    override val jsonKey: String  = "otherAsset"
  }

  def apply(value: String) =
    value match {
      case "cash"           => Cash
      case "property"       => Property
      case "unquotedShares" => UnquotedShares
      case "quotedShares"   => UnquotedShares
      case "other"          => Other
    }

  implicit val reads: Reads[AssetType] =
    Reads {
      case JsString("cash")           => JsSuccess(Cash)
      case JsString("property")       => JsSuccess(Property)
      case JsString("unquotedShares") => JsSuccess(UnquotedShares)
      case JsString("quotedShares")   => JsSuccess(QuotedShares)
      case JsString("other")          => JsSuccess(Other)
      case _                          => JsError("Unable to parse value as AssetType")
    }

  implicit val writes: Writes[AssetType] =
    Writes {
      assetType => JsString(assetType.toString)
    }
}
