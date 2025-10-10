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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class AssetTypeTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer = new AssetTypeTransformer

  "AssetTypeTransformer" - {
    "convert a list of Assets to Json obj of Yes Enum values when present" in {
      val input = Json.obj("transferDetails" ->
        Json.obj("typeOfAsset" -> Seq("cashAssets", "unquotedShareAssets", "quotedShareAssets", "propertyAsset", "otherAsset")))

      val expected = Json.obj("transferDetails" ->
        Json.obj("typeOfAssets" -> Json.obj(
          "cashAssets"          -> "Yes",
          "quotedShareAssets"   -> "Yes",
          "unquotedShareAssets" -> "Yes",
          "propertyAsset"       -> "Yes",
          "otherAsset"          -> "Yes"
        )))

      transformer.construct(input) mustBe Right(expected)
    }

    "convert a list of assets to Json obj with No Enum when values aren't present" in {
      val input = Json.obj("transferDetails" ->
        Json.obj("typeOfAssets" -> Seq.empty[String]))

      val expected = Json.obj("transferDetails" ->
        Json.obj("typeOfAssets" -> Json.obj(
          "cashAssets"          -> "No",
          "quotedShareAssets"   -> "No",
          "unquotedShareAssets" -> "No",
          "propertyAsset"       -> "No",
          "otherAsset"          -> "No"
        )))

      transformer.construct(input) mustBe Right(expected)
    }

    "convert a json object of type of assets to a list of AssetTypes when value is Yes" in {
      val input = Json.obj("transferDetails" ->
        Json.obj("typeOfAssets" -> Json.obj(
          "cashAssets"          -> "Yes",
          "quotedShareAssets"   -> "Yes",
          "unquotedShareAssets" -> "Yes",
          "propertyAsset"       -> "Yes",
          "otherAsset"          -> "Yes",
          "moreProp"            -> "No"
        )))

      val expected = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("moreProp" -> "No"),
          "typeOfAsset"  -> Seq("cashAssets", "unquotedShareAssets", "quotedShareAssets", "propertyAsset", "otherAsset")
        ))

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "convert a json object of type of assets to an empty list of AssetTypes when value is No" in {
      val input = Json.obj("transferDetails" ->
        Json.obj("typeOfAssets" -> Json.obj(
          "cashAssets"          -> "No",
          "quotedShareAssets"   -> "No",
          "unquotedShareAssets" -> "No",
          "propertyAsset"       -> "No",
          "otherAsset"          -> "No"
        )))

      val expected = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj(),
          "typeOfAsset"  -> Seq.empty[String]
        ))

      transformer.deconstruct(input) mustBe Right(expected)
    }
  }
}
