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
import uk.gov.hmrc.overseaspensiontransferbackend.transform.UserAnswersTransformer

class UserAnswersTransformerSpec extends AnyFreeSpec with Matchers {

  "UserAnswersTransformer" - {

    "applyCleanseTransforms should move memberName fields to firstName and lastName" in {
      val input = Json.obj(
        "memberDetails" -> Json.obj(
          "memberName" -> Json.obj(
            "firstName" -> "John",
            "lastName"  -> "Doe"
          )
        )
      )

      val result = UserAnswersTransformer.applyCleanseTransforms(input)

      result mustBe a[Right[_, _]]
      val cleansed = result.toOption.get

      (cleansed \ "memberDetails" \ "firstName").as[String] mustBe "John"
      (cleansed \ "memberDetails" \ "lastName").as[String] mustBe "Doe"
      (cleansed \ "memberDetails" \ "memberName").toOption mustBe None
    }

    "applyEnrichTransforms should move firstName and lastName into memberName object" in {
      val input = Json.obj(
        "memberDetails" -> Json.obj(
          "firstName" -> "Jane",
          "lastName"  -> "Smith"
        )
      )

      val result = UserAnswersTransformer.applyEnrichTransforms(input)

      result mustBe a[Right[_, _]]
      val enriched = result.toOption.get

      (enriched \ "memberDetails" \ "memberName" \ "firstName").as[String] mustBe "Jane"
      (enriched \ "memberDetails" \ "memberName" \ "lastName").as[String] mustBe "Smith"
      (enriched \ "memberDetails" \ "firstName").toOption mustBe empty
      (enriched \ "memberDetails" \ "lastName").toOption mustBe empty
    }

    "applyCleanseTransforms should fail gracefully on invalid input" in {
      val input = Json.obj("memberDetails" -> Json.obj("memberName" -> "notAnObject"))

      val result = UserAnswersTransformer.applyCleanseTransforms(input)

      result mustBe a[Left[_, _]]
      val JsError(errors) = result.left.get
      errors.head._1.toString must include("memberName")
    }

    "applyEnrichTransforms should succeed when no transform needed" in {
      val input = Json.obj("memberDetails" -> Json.obj("memberNino" -> "QQ123456A"))

      val result = UserAnswersTransformer.applyEnrichTransforms(input)

      result mustBe Right(input)
    }
  }
}
