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

class QuotedSharesTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer: QuotedSharesTransformer = new QuotedSharesTransformer

  "UnquotedSharesTransformer" - {
    "convert transferDetails.quotedShares to transferDetails.typeOfAssets.quotedShares" in {
      val input = Json.obj("transferDetails" ->
        Json.obj("quotedShares" -> JsArray(Seq(Json.obj(
          "quotedValue"      -> 12345.01,
          "quotedShareTotal" -> 34,
          "quotedCompany"    -> "BigCompany",
          "quotedClass"      -> "ABC"
        )))))

      val expected = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("quotedShares" -> JsArray(Seq(Json.obj(
            "quotedValue"      -> 12345.01,
            "quotedShareTotal" -> 34,
            "quotedCompany"    -> "BigCompany",
            "quotedClass"      -> "ABC"
          ))))
        ))

      transformer.construct(input) mustBe Right(expected)
    }

    "Update path with new array when already populated" in {
      val newArray = Json.obj("transferDetails" ->
        Json.obj("quotedShares" -> JsArray(Seq(
          Json.obj(
            "quotedValue"      -> 4567.99,
            "quotedShareTotal" -> 12,
            "quotedCompany"    -> "Company Ltd.",
            "quotedClass"      -> "2"
          )
        ))))

      val existingArray = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("quotedShares" -> JsArray(Seq(
            Json.obj(
              "quotedValue"      -> 12345.01,
              "quotedShareTotal" -> 34,
              "quotedCompany"    -> "BigCompany",
              "quotedClass"      -> "ABC"
            ),
            Json.obj(
              "quotedValue"      -> 4567.99,
              "quotedShareTotal" -> 12,
              "quotedCompany"    -> "Company Ltd.",
              "quotedClass"      -> "2"
            )
          )))
        ))

      val input = newArray.deepMerge(existingArray)

      val expected = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("quotedShares" -> JsArray(Seq(
            Json.obj(
              "quotedValue"      -> 4567.99,
              "quotedShareTotal" -> 12,
              "quotedCompany"    -> "Company Ltd.",
              "quotedClass"      -> "2"
            )
          )))
        ))

      transformer.construct(input) mustBe Right(expected)

    }

    "convert transferDetails.typeOfAssets.quotedShares to transferDetails.quotedShares" in {
      val input = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("quotedShares" -> JsArray(Seq(Json.obj(
            "quotedValue"      -> 12345.01,
            "quotedShareTotal" -> 34,
            "quotedCompany"    -> "BigCompany",
            "quotedClass"      -> "ABC"
          ))))
        ))

      val expected = Json.obj("transferDetails" ->
        Json.obj("quotedShares" -> JsArray(Seq(Json.obj(
          "quotedValue"      -> 12345.01,
          "quotedShareTotal" -> 34,
          "quotedCompany"    -> "BigCompany",
          "quotedClass"      -> "ABC"
        )))))

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "must leave JSON unchanged if quotedShares is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj())

      transformer.construct(input) mustBe Right(input)
    }

    "must leave JSON unchanged if transferDetails.typeOfAssets.quotedShares is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj()))

      transformer.deconstruct(input) mustBe Right(input)
    }
  }
}
