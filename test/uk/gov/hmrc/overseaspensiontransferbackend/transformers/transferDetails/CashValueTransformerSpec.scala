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

class CashValueTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer: CashValueTransformer = new CashValueTransformer

  "CashValueTransformer" - {
    "must convert transferDetails.cashValue to transferDetails.typeOfAssets.cashValue on construct" in {
      val input    = Json.obj("transferDetails" -> Json.obj("cashValue" -> 1234567.89))
      val expected = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj("cashValue" -> 1234567.89)))

      transformer.construct(input) mustBe Right(expected)
    }

    "must convert transferDetails.typeOfAssets.cashValue to transferDetails.cashValue on deconstruct" in {
      val input    = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj("cashValue" -> 1234567.89)))
      val expected = Json.obj("transferDetails" -> Json.obj("cashValue" -> 1234567.89))

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "must leave JSON unchanged if cashValue is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj())

      transformer.construct(input) mustBe Right(input)
    }

    "must leave JSON unchanged if transferDetails.typeOfAssets.cashValue is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj()))

      transformer.deconstruct(input) mustBe Right(input)
    }
  }
}
