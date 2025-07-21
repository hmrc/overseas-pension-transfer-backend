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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Reads.StringReads
import play.api.libs.json._

class EnumTransformerStepSpec extends AnyFreeSpec with Matchers with EnumTransformerStep {

  private val path: JsPath = __ \ "memberDetails" \ "memberType"

  "EnumTransformerStep" - {
    "convert given type to JsString" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("memberType" -> JsString("Hello")))

      val expected = Json.obj("memberDetails" -> Json.obj("memberType" -> JsString("Bonjour")))

      val frenchify: String => JsString = _ => JsString("Bonjour")

      constructEnum[String](path, frenchify)(StringReads)(inputJson) mustBe Right(expected)
    }

    "convert JsString to given type string value" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj("memberType" -> JsString("One hundred and forty four")))

      val expected = Json.obj("memberDetails" -> Json.obj("memberType" -> JsString("144")))

      val numConversion: String => JsString = _ => JsString(144.toString)

      constructEnum[String](path, numConversion)(StringReads)(inputJson) mustBe Right(expected)
    }
  }

}
