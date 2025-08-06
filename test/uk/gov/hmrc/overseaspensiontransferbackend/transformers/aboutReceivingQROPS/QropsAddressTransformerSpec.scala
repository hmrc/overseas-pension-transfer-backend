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
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json._

class QropsAddressTransformerSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  private val transformer = new QropsAddressTransformer

  "QropsAddressTransformer" - {

    "must move and wrap qropsDetails.receivingQropsAddress into aboutReceivingQROPS.receivingQropsAddress" in {
      val inputJson = Json.obj(
        "qropsDetails" -> Json.obj(
          "receivingQropsAddress" -> Json.obj(
            "addressLine1" -> "123 Test St",
            "addressLine2" -> "Testville",
            "addressLine3" -> "Testshire",
            "ukPostCode"   -> "TE5 7ST",
            "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
          )
        )
      )

      val expected = Json.obj(
        "aboutReceivingQROPS" -> Json.obj(
          "receivingQropsAddress" -> Json.obj(
            "addressLine1" -> "123 Test St",
            "addressLine2" -> "Testville",
            "addressLine3" -> "Testshire",
            "ukPostCode"   -> "TE5 7ST",
            "country"      -> "GB"
          )
        )
      )

      val result = transformer.construct(inputJson)
      result mustBe Right(expected)
    }

    "must unwrap aboutReceivingQROPS.receivingQropsAddress back to qropsDetails.receivingQropsAddress" in {
      val inputJson = Json.obj(
        "aboutReceivingQROPS" -> Json.obj(
          "receivingQropsAddress" -> Json.obj(
            "addressLine1" -> "123 Test St",
            "addressLine2" -> "Testville",
            "addressLine3" -> "Testshire",
            "ukPostCode"   -> "TE5 7ST",
            "country"      -> "GB"
          )
        )
      )

      val expected = Json.obj(
        "qropsDetails" -> Json.obj(
          "receivingQropsAddress" -> Json.obj(
            "addressLine1" -> "123 Test St",
            "addressLine2" -> "Testville",
            "addressLine3" -> "Testshire",
            "ukPostCode"   -> "TE5 7ST",
            "country"      -> Json.obj("code" -> "GB")
          )
        )
      )

      val result = transformer.deconstruct(inputJson)
      result mustBe Right(expected)
    }

    "must leave JSON unchanged if qropsDetails.receivingQropsAddress is missing" in {
      val inputJson = Json.obj("qropsDetails" -> Json.obj())

      val result = transformer.construct(inputJson)
      result mustBe Right(Json.obj("qropsDetails" -> Json.obj()))
    }

    "must leave JSON unchanged if aboutReceivingQROPS.receivingQropsAddress is missing" in {
      val inputJson = Json.obj(
        "aboutReceivingQROPS" -> Json.obj()
      )
      transformer.deconstruct(inputJson) mustBe Right(inputJson)
    }
  }
}
