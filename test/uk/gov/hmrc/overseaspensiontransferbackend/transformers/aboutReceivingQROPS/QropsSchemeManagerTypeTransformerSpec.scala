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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.aboutReceivingQROPS

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class QropsSchemeManagerTypeTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer = new QropsSchemeManagerTypeTransformer

  "QropsSchemeManagerTypeTransformerSpec" - {
    "must move schemeManagerDetails.schemeManagerType to aboutReceivingQrops.qropsSchemeManagerType.schemeManagerType" in {
      val inputJson = Json.obj("schemeManagerDetails" -> Json.obj("schemeManagerType" -> "individual"))
      val expected  = Json.obj("aboutReceivingQROPS" -> Json.obj("qropsSchemeManagerType" -> Json.obj("schemeManagerType" -> "individual")))

      transformer.construct(inputJson) mustBe Right(expected)
    }

    "must move aboutReceivingQrops.qropsSchemeManagerType.schemeManagerType to schemeManagerDetails.schemeManagerType" in {
      val inputJson = Json.obj("aboutReceivingQROPS" -> Json.obj("qropsSchemeManagerType" -> Json.obj("schemeManagerType" -> "individual")))
      val expected  = Json.obj("schemeManagerDetails" -> Json.obj("schemeManagerType" -> "individual"))

      transformer.deconstruct(inputJson) mustBe Right(expected)
    }

    "must leave Json unchanged if schemeManagerDetails.schemeManagerType is missing" in {
      val inputJson = Json.obj("schemeManagerDetails" -> Json.obj())

      transformer.construct(inputJson) mustBe Right(inputJson)
    }

    "must leave Json unchanged if aboutReceivingQrops.qropsSchemeManagerType is missing" in {
      val inputJson = Json.obj("abputReceeivingQrops" -> Json.obj())

      transformer.deconstruct(inputJson) mustBe Right(inputJson)
    }
  }
}
