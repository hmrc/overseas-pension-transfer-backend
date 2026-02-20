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
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase

class MemberNinoTransformerSpec extends AnyFreeSpec with Matchers with SpecBase {

  private val transformer = new MemberNinoTransformer

  "MemberNinoTransformer" - {

    "must move memberDetails.nino to transferringMember.memberDetails.nino" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("nino" -> testNino))
      val expected  = Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj("nino" -> testNino)))

      transformer.construct(inputJson) mustBe Right(expected)
    }

    "must move transferringMember.memberDetails.nino to memberDetails.nino" in {
      val inputJson = Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj("nino" -> testNino)))
      val expected  = Json.obj("memberDetails" -> Json.obj("nino" -> testNino))

      transformer.deconstruct(inputJson) mustBe Right(expected)
    }

    "must leave JSON unchanged if memberDetails.nino is missing" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj())

      transformer.construct(inputJson) mustBe Right(inputJson)
    }

    "must leave JSON unchanged if transferringMember.memberDetails.nino is missing" in {
      val inputJson = Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj()))

      transformer.deconstruct(inputJson) mustBe Right(inputJson)
    }
  }
}
