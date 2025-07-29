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

class MoreOtherAssetTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer: MoreOtherAssetTransformer = new MoreOtherAssetTransformer

  "MoreOtherAssetTransformer" - {
    "must convert transferDetails.moreAsset to transferDetails.typeOfAssets.moreAsset boolean to nested Yes/No string" in {
      val input    = Json.obj("transferDetails" -> Json.obj("moreAsset" -> true))
      val expected = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj("moreAsset" -> "Yes")))

      transformer.construct(input) mustBe Right(expected)
    }

    "must convert nested Yes/No string back to boolean" in {
      val input    = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj("moreAsset" -> "No")))
      val expected = Json.obj("transferDetails" -> Json.obj("moreAsset" -> false))

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "must leave JSON unchanged if moreAsset is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj())

      transformer.construct(input) mustBe Right(input)
    }

    "must leave JSON unchanged if transferDetails.typeOfAssets.moreAsset is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj()))

      transformer.deconstruct(input) mustBe Right(input)
    }
  }
}
