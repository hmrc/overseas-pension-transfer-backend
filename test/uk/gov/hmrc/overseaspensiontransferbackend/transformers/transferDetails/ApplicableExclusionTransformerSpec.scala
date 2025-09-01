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

class ApplicableExclusionTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer = new ApplicableExclusionTransformer

  "ApplicableExclusionTransfromerSpec" - {
    "must move transferDetails.applicableExclusion to transferDetails.taxableOverseasTransferDetails.applicableExclusion" in {
      val inputJson = Json.obj("transferDetails" -> Json.obj("applicableExclusion" -> Seq("publicService")))
      val expected  = Json.obj("transferDetails" -> Json.obj("taxableOverseasTransferDetails" -> Json.obj("applicableExclusion" -> Seq("02"))))

      transformer.construct(inputJson) mustBe Right(expected)
    }

    "must move transferDetails.taxableOverseasTransferDetails.applicableExclusion to transferDetails.applicableExclusion" in {
      val inputJson = Json.obj("transferDetails" -> Json.obj("taxableOverseasTransferDetails" -> Json.obj("applicableExclusion" -> Seq("01", "04"))))
      val expected  = Json.obj("transferDetails" -> Json.obj("applicableExclusion" -> Seq("occupational", "resident")))

      transformer.deconstruct(inputJson) mustBe Right(expected)
    }

    "must leave Json unchanged if transferDetails.applicableExclusion is missing" in {
      val inputJson = Json.obj("transferDetails" -> Json.obj())

      transformer.construct(inputJson) mustBe Right(inputJson)
    }

    "must leave Json unchanged if transferDetails.taxableOverseasTransferDetails.applicableExclusion is missing" in {
      val inputJson = Json.obj("transferDetails" -> Json.obj("taxableOverseasTransferDetails" -> Json.obj()))

      transformer.deconstruct(inputJson) mustBe Right(inputJson)
    }
  }
}
