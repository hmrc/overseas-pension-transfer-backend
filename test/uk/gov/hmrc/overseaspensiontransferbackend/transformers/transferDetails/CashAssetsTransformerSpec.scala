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

class CashAssetsTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer: CashAssetsTransformer = new CashAssetsTransformer

  "CashAssetsTransformer" - {

    "must move from transferDetails.cashAsset to transferDetails.typeOfAssets.cashAssets and convert boolean to nested Yes/No string" in {
      val input    = Json.obj("transferDetails" -> Json.obj("cashOnlyTransfer" -> "Yes"))
      val expected = Json.obj("transferDetails" -> Json.obj("cashOnlyTransfer" -> "Yes", "typeOfAssets" -> Json.obj("cashAssets" -> "Yes")))

      transformer.construct(input) mustBe Right(expected)
    }

    "must not set cashAssets if cashOnlyValue is false" in {
      val input = Json.obj("transferDetails" -> Json.obj("cashOnlyTransfer" -> "No"))

      transformer.construct(input) mustBe Right(input)
    }

    "must convert nested Yes/No string back to boolean and persist" in {
      val input    = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj("cashAssets" -> "No")))
      val expected = Json.obj("transferDetails" -> Json.obj("cashOnlyTransfer" -> false, "typeOfAssets" -> Json.obj("cashAssets" -> "No")))

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "must set cashOnlyTransfer as false when cashAssets = Yes and other assets = Yes" in {
      val input    = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj("cashAssets" -> "Yes", "propertyAsset" -> "Yes")))
      val expected = Json.obj(
        "transferDetails" ->
          Json.obj(
            "cashOnlyTransfer" -> false,
            "typeOfAssets"     -> Json.obj(
              "cashAssets"    -> "Yes",
              "propertyAsset" -> "Yes"
            )
          )
      )

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "must leave JSON unchanged if cashOnlyTransfer is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj())

      transformer.construct(input) mustBe Right(input)
    }

    "must leave JSON unchanged if cashOnlyTransfer.typeOfAssets.cashAsset is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj()))

      transformer.construct(input) mustBe Right(input)
    }
  }

}
