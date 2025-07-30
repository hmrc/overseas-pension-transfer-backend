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

class PropertyTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer: PropertyTransformer = new PropertyTransformer

  private val address = Json.obj(
    "addressLine1" -> "",
    "addressLine2" -> "",
    "addressLine3" -> "",
    "addressLine4" -> "",
    "addressLine5" -> "",
    "ukPostcode"   -> "",
    "country"      -> Json.obj("name" -> "", "code" -> "")
  )

  "UnquotedSharesTransformer" - {
    "convert transferDetails.propertyAssets to transferDetails.typeOfAssets.propertyAssets" in {
      val input = Json.obj("transferDetails" ->
        Json.obj("propertyAssets" -> JsArray(Seq(Json.obj(
          "propertyAddress" -> address,
          "propValue"       -> 123456.00,
          "propDescription" -> "Buckingham Palace"
        )))))

      val expected = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("propertyAssets" -> JsArray(Seq(Json.obj(
            "propertyAddress" -> address,
            "propValue"       -> 123456.00,
            "propDescription" -> "Buckingham Palace"
          ))))
        ))

      transformer.construct(input) mustBe Right(expected)
    }

    "Update path with new array when already populated" in {
      val newArray = Json.obj("transferDetails" ->
        Json.obj("propertyAssets" -> JsArray(Seq(
          Json.obj(
            "propertyAddress" -> address,
            "propValue"       -> 123456.00,
            "propDescription" -> "Buckingham Palace"
          ),
          Json.obj(
            "propertyAddress" -> address,
            "propValue"       -> 650000.00,
            "propDescription" -> "Countryside Pub"
          )
        ))))

      val existingArray = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("propertyAssets" -> JsArray(Seq(Json.obj(
            "propertyAddress" -> address,
            "propValue"       -> 123456.00,
            "propDescription" -> "Buckingham Palace"
          ))))
        ))

      val input = newArray.deepMerge(existingArray)

      val expected = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("propertyAssets" -> JsArray(Seq(
            Json.obj(
              "propertyAddress" -> address,
              "propValue"       -> 123456.00,
              "propDescription" -> "Buckingham Palace"
            ),
            Json.obj(
              "propertyAddress" -> address,
              "propValue"       -> 650000.00,
              "propDescription" -> "Countryside Pub"
            )
          )))
        ))

      transformer.construct(input) mustBe Right(expected)

    }

    "convert transferDetails.typeOfAssets.propertyAssets to transferDetails.propertyAssets" in {
      val input = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("propertyAssets" -> JsArray(Seq(Json.obj(
            "propertyAddress" -> address,
            "propValue"       -> 123456.00,
            "propDescription" -> "Buckingham Palace"
          ))))
        ))

      val expected = Json.obj("transferDetails" ->
        Json.obj("propertyAssets" -> JsArray(Seq(Json.obj(
          "propertyAddress" -> address,
          "propValue"       -> 123456.00,
          "propDescription" -> "Buckingham Palace"
        )))))

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "must leave JSON unchanged if propertyAssets is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj())

      transformer.construct(input) mustBe Right(input)
    }

    "must leave JSON unchanged if transferDetails.typeOfAssets.propertyAssets is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj()))

      transformer.deconstruct(input) mustBe Right(input)
    }
  }
}
