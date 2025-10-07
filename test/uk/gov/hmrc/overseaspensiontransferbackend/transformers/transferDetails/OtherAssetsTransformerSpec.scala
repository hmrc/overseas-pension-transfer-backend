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
import play.api.libs.json.{JsArray, Json}

class OtherAssetsTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer: OtherAssetsTransformer = new OtherAssetsTransformer

  "OtherAssetsTransformer" - {
    "convert transferDetails.otherAssets to transferDetails.typeOfAssets.otherAssets" in {
      val input = Json.obj("transferDetails" ->
        Json.obj("otherAsset" -> JsArray(Seq(Json.obj(
          "assetValue"       -> 985421.87,
          "assetDescription" -> "bakery business"
        )))))

      val expected = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("otherAsset" -> JsArray(Seq(Json.obj(
            "assetValue"       -> 985421.87,
            "assetDescription" -> "bakery business"
          ))))
        ))

      transformer.construct(input) mustBe Right(expected)
    }

    "Update path with new array when already populated" in {
      val newArray = Json.obj("transferDetails" ->
        Json.obj("otherAssets" -> JsArray(Seq(
          Json.obj(
            "assetValue"       -> 985421.87,
            "assetDescription" -> "bakery business"
          ),
          Json.obj(
            "assetValue"       -> 11110.80,
            "assetDescription" -> "plate collection"
          )
        ))))

      val existingArray = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("otherAssets" -> JsArray(Seq(Json.obj(
            "assetValue"       -> 985421.87,
            "assetDescription" -> "bakery business"
          ))))
        ))

      val input = newArray.deepMerge(existingArray)

      val expected = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("otherAssets" -> JsArray(Seq(
            Json.obj(
              "assetValue"       -> 985421.87,
              "assetDescription" -> "bakery business"
            ),
            Json.obj(
              "assetValue"       -> 11110.80,
              "assetDescription" -> "plate collection"
            )
          )))
        ))

      transformer.construct(input) mustBe Right(expected)

    }

    "convert transferDetails.typeOfAssets.otherAssets to transferDetails.otherAssets" in {
      val input = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("otherAssets" -> JsArray(Seq(Json.obj(
            "assetValue"       -> 985421.87,
            "assetDescription" -> "bakery business"
          ))))
        ))

      val expected = Json.obj("transferDetails" ->
        Json.obj("otherAssets" -> JsArray(Seq(Json.obj(
          "assetValue"       -> 985421.87,
          "assetDescription" -> "bakery business"
        )))))

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "must leave JSON unchanged if otherAssets is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj())

      transformer.construct(input) mustBe Right(input)
    }

    "must leave JSON unchanged if transferDetails.typeOfAssets.otherAssets is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj()))

      transformer.deconstruct(input) mustBe Right(input)
    }
  }
}
