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

class UnquotedSharesTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer: UnquotedSharesTransformer = new UnquotedSharesTransformer

  "UnquotedSharesTransformer" - {
    "convert transferDetails.unquotedShares to transferDetails.typeOfAssets.unquotedShares" in {
      val input = Json.obj("transferDetails" ->
        Json.obj("unquotedShares" -> JsArray(Seq(Json.obj(
          "unquotedValue"      -> 12345.01,
          "unquotedShareTotal" -> 34,
          "unquotedCompany"    -> "BigCompany",
          "unquotedClass"      -> "ABC"
        )))))

      val expected = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("unquotedShares" -> JsArray(Seq(Json.obj(
            "unquotedValue"      -> 12345.01,
            "unquotedShareTotal" -> 34,
            "unquotedCompany"    -> "BigCompany",
            "unquotedClass"      -> "ABC"
          ))))
        ))

      transformer.construct(input) mustBe Right(expected)
    }

    "Update path with new array when already populated" in {
      val newArray = Json.obj("transferDetails" ->
        Json.obj("unquotedShares" -> JsArray(Seq(
          Json.obj(
            "unquotedValue"      -> 12345.01,
            "unquotedShareTotal" -> 34,
            "unquotedCompany"    -> "BigCompany",
            "unquotedClass"      -> "ABC"
          ),
          Json.obj(
            "unquotedValue"      -> 4567.99,
            "unquotedShareTotal" -> 12,
            "unquotedCompany"    -> "Company Ltd.",
            "unquotedClass"      -> "2"
          )
        ))))

      val existingArray = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("unquotedShares" -> JsArray(Seq(Json.obj(
            "unquotedValue"      -> 12345.01,
            "unquotedShareTotal" -> 34,
            "unquotedCompany"    -> "BigCompany",
            "unquotedClass"      -> "ABC"
          ))))
        ))

      val input = newArray.deepMerge(existingArray)

      val expected = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("unquotedShares" -> JsArray(Seq(
            Json.obj(
              "unquotedValue"      -> 12345.01,
              "unquotedShareTotal" -> 34,
              "unquotedCompany"    -> "BigCompany",
              "unquotedClass"      -> "ABC"
            ),
            Json.obj(
              "unquotedValue"      -> 4567.99,
              "unquotedShareTotal" -> 12,
              "unquotedCompany"    -> "Company Ltd.",
              "unquotedClass"      -> "2"
            )
          )))
        ))

      transformer.construct(input) mustBe Right(expected)

    }

    "convert transferDetails.typeOfAssets.unquotedShares to transferDetails.unquotedShares" in {
      val input = Json.obj("transferDetails" ->
        Json.obj(
          "typeOfAssets" -> Json.obj("unquotedShares" -> JsArray(Seq(Json.obj(
            "unquotedValue"      -> 12345.01,
            "unquotedShareTotal" -> 34,
            "unquotedCompany"    -> "BigCompany",
            "unquotedClass"      -> "ABC"
          ))))
        ))

      val expected = Json.obj("transferDetails" ->
        Json.obj("unquotedShares" -> JsArray(Seq(Json.obj(
          "unquotedValue"      -> 12345.01,
          "unquotedShareTotal" -> 34,
          "unquotedCompany"    -> "BigCompany",
          "unquotedClass"      -> "ABC"
        )))))

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "must leave JSON unchanged if unquotedShares is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj())

      transformer.construct(input) mustBe Right(input)
    }

    "must leave JSON unchanged if transferDetails.typeOfAssets.unquotedShares is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj()))

      transformer.deconstruct(input) mustBe Right(input)
    }
  }
}
