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

class QropsSchemeManagerPhoneTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer = new QropsSchemeManagerPhoneTranformer

  "QropsSchemeManagerEmailTransformer" - {

    "must move schemeManagerDetails.schemeManagerPhone to aboutReceivingQROPS.qropsSchemeManagerType.schemeManagerPhone" in {
      val inputJson = Json.obj("schemeManagerDetails" -> Json.obj("schemeManagerPhone" -> "07777777777"))
      val expected  = Json.obj("aboutReceivingQROPS" -> Json.obj("qropsSchemeManagerType" -> Json.obj("schemeManagerPhone" -> "07777777777")))

      transformer.construct(inputJson) mustBe Right(expected)
    }

    "must move aboutReceivingQROPS.qropsSchemeManagerType.schemeManagerPhone to schemeManagerDetails.schemeManagerPhone" in {
      val inputJson = Json.obj("aboutReceivingQROPS" -> Json.obj("qropsSchemeManagerType" -> Json.obj("schemeManagerPhone" -> "07777777777")))
      val expected  = Json.obj("schemeManagerDetails" -> Json.obj("schemeManagerPhone" -> "07777777777"))

      transformer.deconstruct(inputJson) mustBe Right(expected)
    }

    "must leave JSON unchanged if schemeManagerDetails.schemeManagerPhone is missing" in {
      val inputJson = Json.obj("schemeManagerDetails" -> Json.obj())

      transformer.construct(inputJson) mustBe Right(inputJson)
    }

    "must leave JSON unchanged if aboutReceivingQROPS.qropsSchemeManagerType.schemeManagerPhone is missing" in {
      val inputJson = Json.obj("aboutReceivingQROPS" -> Json.obj())

      transformer.deconstruct(inputJson) mustBe Right(inputJson)
    }
  }
}
