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

class TransferMinusTaxTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer = new TransferMinusTaxTransformer

  "AmountTaxDeductedTransformerSpec" - {
    "must move transferDetails.transferMinusTax to transferDetails.taxableOverseasTransferDetails.transferMinusTax" in {
      val inputJson = Json.obj("transferDetails" -> Json.obj("transferMinusTax" -> 12345.99))
      val expected  = Json.obj("transferDetails" -> Json.obj("taxableOverseasTransferDetails" -> Json.obj("transferMinusTax" -> 12345.99)))

      transformer.construct(inputJson) mustBe Right(expected)
    }

    "must move transferDetails.taxableOverseasTransferDetails.transferMinusTax to transferDetails.transferMinusTax" in {
      val inputJson = Json.obj("transferDetails" -> Json.obj("taxableOverseasTransferDetails" -> Json.obj("transferMinusTax" -> 100000.00)))
      val expected  = Json.obj("transferDetails" -> Json.obj("transferMinusTax" -> 100000.00))

      transformer.deconstruct(inputJson) mustBe Right(expected)
    }

    "must leave Json unchanged if transferDetails is missing" in {
      val inputJson = Json.obj("transferDetails" -> Json.obj())

      transformer.construct(inputJson) mustBe Right(inputJson)
    }

    "must leave Json unchanged if transferDetails.taxableOverseasTransferDetails.transferMinusTax is missing" in {
      val inputJson = Json.obj("transferDetails" -> Json.obj("taxableOverseasTransferDetails" -> Json.obj()))

      transformer.deconstruct(inputJson) mustBe Right(inputJson)
    }
  }
}
