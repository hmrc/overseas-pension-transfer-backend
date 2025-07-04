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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.memberDetails

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferringMember.memberDetails.MemberNameTransformerStep

class MemberNameTransformerStepSpec extends AnyFreeSpec with Matchers {

  private val transformer = new MemberNameTransformerStep

  "MemberNameTransformerStep" - {

    "cleanse" - {

      "should flatten memberName into foreName and lastName" in {
        val input = Json.obj("memberDetails" ->
          Json.obj(
            "memberName"  -> Json.obj(
              "firstName" -> JsString("Alice"),
              "lastName"  -> JsString("Walker")
            ),
            "dateOfBirth" -> "1990-01-01"
          ))

        val result = transformer.cleanse(input)

        result mustBe a[Right[_, _]]
        val output = result.toOption.get

        (output \ "memberDetails" \ "foreName").as[String] mustBe "Alice"
        (output \ "memberDetails" \ "lastName").as[String] mustBe "Walker"
        (output \ "memberDetails" \ "dateOfBirth").as[String] mustBe "1990-01-01"
        (output \ "memberDetails" \ "memberName").toOption mustBe None
      }

      "should ignore if memberName is missing" in {
        val input = Json.obj("foo" -> "bar")

        val result = transformer.cleanse(input)

        result mustBe Right(input)
      }
    }

    "enrich" - {

      "should nest foreName and lastName into memberName" in {
        val input = Json.obj(
          "foreName" -> "John",
          "lastName" -> "Doe",
          "extra"    -> "data"
        )

        val result = transformer.enrich(input)

        result mustBe a[Right[_, _]]
        val output = result.toOption.get

        (output \ "memberName" \ "firstName").as[String] mustBe "John"
        (output \ "memberName" \ "lastName").as[String] mustBe "Doe"
        (output \ "extra").as[String] mustBe "data"
        (output \ "foreName").toOption mustBe None
        (output \ "lastName").toOption mustBe None
      }

      "should insert memberName if only one of foreName or lastName exists" in {
        val input = Json.obj("foreName" -> "OnlyFirst")

        val result = transformer.enrich(input)

        result mustBe a[Right[_, _]]
        val output = result.toOption.get

        (output \ "memberName" \ "firstName").as[String] mustBe "OnlyFirst"
        (output \ "memberName" \ "lastName").toOption must contain(JsNull)
      }

      "should return input unchanged if neither foreName nor lastName exist" in {
        val input = Json.obj("somethingElse" -> "yes")

        val result = transformer.enrich(input)

        result mustBe Right(input)
      }
    }
  }
}
