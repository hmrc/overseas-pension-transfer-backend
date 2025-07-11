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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferringMember

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._

class MemberEverUKResidentTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer = new MemberEverUKResidentTransformer

  "MemberEverUKResidentTransformer" - {

    "must move and convert memberDetails.memEverUkResident boolean to nested Yes/No string" in {
      val input    = Json.obj("memberDetails" -> Json.obj("memEverUkResident" -> true))
      val expected = Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj("memberResidencyDetails" -> Json.obj("memEverUkResident" -> "Yes"))))

      transformer.construct(input) mustBe Right(expected)
    }

    "must move and convert nested Yes/No string back to boolean" in {
      val input    = Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj("memberResidencyDetails" -> Json.obj("memEverUkResident" -> "No"))))
      val expected = Json.obj("memberDetails" -> Json.obj("memEverUkResident" -> false))

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "must leave JSON unchanged if memEverUkResident is missing" in {
      val input = Json.obj("memberDetails" -> Json.obj())

      transformer.construct(input) mustBe Right(input)
    }

    "must leave JSON unchanged if memEverUkResident is missing in nested structure" in {
      val input = Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj("memberResidencyDetails" -> Json.obj())))

      transformer.deconstruct(input) mustBe Right(input)
    }
  }
}
