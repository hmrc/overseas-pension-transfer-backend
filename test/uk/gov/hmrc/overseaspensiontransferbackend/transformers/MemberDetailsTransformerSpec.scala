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
import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.transform.MemberDetailsTransformer

class MemberDetailsTransformerSpec extends AnyFreeSpec with Matchers {

  "MemberDetailsTransformer" - {

    "cleanse should flatten memberName into firstName and lastName" in {
      val input = Json.obj("memberDetails" -> Json.obj(
        "memberName" -> Json.obj("firstName" -> "Alice", "lastName" -> "Walker")
      ))

      val result = MemberDetailsTransformer.cleanse(input)

      result mustBe a[Right[_, _]]
      val output = result.toOption.get

      (output \ "memberDetails" \ "firstName").as[String] mustBe "Alice"
      (output \ "memberDetails" \ "lastName").as[String] mustBe "Walker"
      (output \ "memberDetails" \ "memberName").toOption mustBe None
    }

    "cleanse should fail if memberName is not an object" in {
      val input = Json.obj("memberDetails" -> Json.obj("memberName" -> JsString("bad")))

      val result = MemberDetailsTransformer.cleanse(input)

      result mustBe a[Left[_, _]]
    }

    "enrich should nest firstName and lastName into memberName" in {
      val input = Json.obj("memberDetails" -> Json.obj(
        "firstName" -> "Bob",
        "lastName"  -> "Hope"
      ))

      val result = MemberDetailsTransformer.enrich(input)

      result mustBe a[Right[_, _]]
      val output = result.toOption.get

      (output \ "memberDetails" \ "memberName" \ "firstName").as[String] mustBe "Bob"
      (output \ "memberDetails" \ "memberName" \ "lastName").as[String] mustBe "Hope"
      (output \ "memberDetails" \ "firstName").toOption mustBe empty
    }

    "enrich should return input unchanged if no first/last name" in {
      val input = Json.obj("memberDetails" -> Json.obj("memberNino" -> "QQ123456A"))

      val result = MemberDetailsTransformer.enrich(input)

      result mustBe Right(input)
    }
  }
}
