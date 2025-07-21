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
import play.api.libs.json._

class QropsEstablishedOtherTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer = new QropsEstablishedOtherTransformer

  "QropsEstablishedOtherTransformer" - {

    "must move qropsDetails.qropsEstablishedOther to aboutReceivingQROPS.receivingQropsEstablishedDetails.qropsEstablishedOther" in {
      val inputJson = Json.obj("qropsDetails" -> Json.obj("qropsEstablishedOther" -> "Other"))
      val expected  = Json.obj("aboutReceivingQROPS" ->
        Json.obj("receivingQropsEstablishedDetails" ->
          Json.obj("qropsEstablishedOther" -> "Other")))

      transformer.construct(inputJson) mustBe Right(expected)
    }

    "must remove qropsEstablished if qropsEstablishedOther is being set" in {
      val incomingFrontendJson = Json.obj(
        "qropsDetails" -> Json.obj("qropsEstablishedOther" -> "Other")
      )

      val existingInternalJson = Json.obj(
        "aboutReceivingQROPS" -> Json.obj(
          "receivingQropsEstablishedDetails" -> Json.obj(
            "qropsEstablished" -> Json.obj("code" -> "FR", "name" -> "France")
          )
        )
      )

      val mergedInput = existingInternalJson.deepMerge(incomingFrontendJson)

      val expected = Json.obj(
        "aboutReceivingQROPS" -> Json.obj(
          "receivingQropsEstablishedDetails" -> Json.obj(
            "qropsEstablishedOther" -> "Other"
          )
        )
      )

      transformer.construct(mergedInput) mustBe Right(expected)
    }

    "must preserve qropsEstablished if qropsEstablishedOther is NOT being set" in {
      val incomingFrontendJson = Json.obj(
        "qropsDetails" -> Json.obj()
      )

      val existingInternalJson = Json.obj(
        "aboutReceivingQROPS" -> Json.obj(
          "receivingQropsEstablishedDetails" -> Json.obj(
            "qropsEstablished" -> Json.obj("code" -> "FR", "name" -> "France")
          )
        )
      )

      val mergedInput = existingInternalJson.deepMerge(incomingFrontendJson)

      transformer.construct(mergedInput) mustBe Right(mergedInput)
    }

    "must update aboutReceivingQROPS.receivingQropsEstablishedDetails.qropsEstablishedOther if value is already set" in {
      val incomingFrontendJson = Json.obj(
        "qropsDetails" -> Json.obj("qropsEstablishedOther" -> "Updated Other")
      )

      val existingInternalJson = Json.obj(
        "aboutReceivingQROPS" -> Json.obj(
          "receivingQropsEstablishedDetails" -> Json.obj(
            "qropsEstablishedOther" -> "Something Else"
          )
        )
      )

      val mergedInput = existingInternalJson.deepMerge(incomingFrontendJson)

      val expected = Json.obj(
        "aboutReceivingQROPS" -> Json.obj(
          "receivingQropsEstablishedDetails" -> Json.obj(
            "qropsEstablishedOther" -> "Updated Other"
          )
        )
      )

      transformer.construct(mergedInput) mustBe Right(expected)
    }

    "must move aboutReceivingQROPS.receivingQropsEstablishedDetails.qropsEstablishedOther to qropsDetails.qropsEstablishedOther" in {
      val inputJson = Json.obj("aboutReceivingQROPS" ->
        Json.obj("receivingQropsEstablishedDetails" ->
          Json.obj("qropsEstablishedOther" -> "Other")))
      val expected  = Json.obj("qropsDetails" -> Json.obj("qropsEstablishedOther" -> "Other"))

      transformer.deconstruct(inputJson) mustBe Right(expected)
    }

    "must leave JSON unchanged if qropsDetails.qropsEstablishedOther is missing" in {
      val inputJson = Json.obj("qropsDetails" -> Json.obj())

      transformer.construct(inputJson) mustBe Right(inputJson)
    }

    "must leave JSON unchanged if aboutReceivingQROPS.receivingQropsEstablishedDetails.qropsEstablishedOther is missing" in {
      val inputJson = Json.obj("aboutReceivingQROPS" -> Json.obj("receivingQropsEstablishedDetails" -> Json.obj()))

      transformer.deconstruct(inputJson) mustBe Right(inputJson)
    }
  }
}
