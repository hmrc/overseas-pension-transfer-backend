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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsError, JsObject, Json}

class TransferringMemberTransformerSpec extends AnyFreeSpec with Matchers with MockitoSugar {
  val transformer                                  = new TransferringMemberTransformer
  val incomingJsonMemberDetails: JsObject          = Json.obj("memberDetails" -> Json.obj("memberName" -> Json.obj("firstName" -> "Bill", "lastName" -> "Withers")))
  val incomingJsonMemberResidencyDetails: JsObject = Json.obj("memberDetails" -> Json.obj("memberResidencyDetails" -> Json.obj("memUkResident" -> true)))
  val missingMemDetailsJson: JsObject              = Json.obj("Hello there" -> Json.obj("General Kenobi" -> true))

  "TransferringMemberTransformer" - {

    "should construct transferringMember json from memberDetails json" in {
      transformer.construct(incomingJsonMemberDetails) mustBe Right(Json.obj(
        "transferringMember" -> incomingJsonMemberDetails
      ))
    }

    "should deconstruct memberDetails json from transferringMember" in {
      val constructed = transformer.construct(incomingJsonMemberDetails)
      constructed.flatMap(transformer.deconstruct) mustBe Right(incomingJsonMemberDetails)
    }

    "should construct transferringMember json from memberResidencyDetails Json" in {
      transformer.construct(incomingJsonMemberResidencyDetails) mustBe Right(Json.obj(
        "transferringMember" -> incomingJsonMemberResidencyDetails
      ))
    }

    "should deconstruct memberResidencyDetails json from transferringMember" in {
      val constructed = transformer.construct(incomingJsonMemberResidencyDetails)
      constructed.flatMap(transformer.deconstruct) mustBe Right(incomingJsonMemberResidencyDetails)
    }

    "should return a left JsError from deconstruct if transferringMember does not exist" in {
      transformer.deconstruct(missingMemDetailsJson) mustBe Left(JsError("memberDetails does not exist"))
    }
  }
}
