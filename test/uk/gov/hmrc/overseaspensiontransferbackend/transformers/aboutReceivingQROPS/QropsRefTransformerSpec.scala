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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class QropsRefTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer = new QropsRefTransformer

  "QropsNameTransformer" - {

    "must move qropsDetails.qropsRef to aboutReceivingQROPS.qropsRef" in {
      val inputJson = Json.obj("qropsDetails" -> Json.obj("qropsRef" -> "AB123"))
      val expected  = Json.obj("aboutReceivingQROPS" -> Json.obj("qropsRef" -> "AB123"))

      transformer.construct(inputJson) mustBe Right(expected)
    }

    "must move aboutReceivingQROPS.qropsRef to qropsDetails.qropsRef" in {
      val inputJson = Json.obj("aboutReceivingQROPS" -> Json.obj("qropsRef" -> "AB123"))
      val expected  = Json.obj("qropsDetails" -> Json.obj("qropsRef" -> "AB123"))

      transformer.deconstruct(inputJson) mustBe Right(expected)
    }

    "must leave JSON unchanged if qropsDetails.qropsRef is missing" in {
      val inputJson = Json.obj("qropsDetails" -> Json.obj())

      transformer.construct(inputJson) mustBe Right(inputJson)
    }

    "must leave JSON unchanged if aboutReceivingQROPS.qropsRef is missing" in {
      val inputJson = Json.obj("aboutReceivingQROPS" -> Json.obj())

      transformer.deconstruct(inputJson) mustBe Right(inputJson)
    }
  }
}
