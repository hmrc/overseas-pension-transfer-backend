/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.overseaspensiontransferbackend.validators

import play.api.mvc.Results.{InternalServerError, Ok}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase

import scala.concurrent.Future

class PathParameterValidatorSpec extends AnyFreeSpec with Matchers with SpecBase {
  "withPstrNumber" - {
    "Returns Internal server error for invalid pstr" in {
      def sampleBlock(s: String) = Future.successful(Ok(s))
      val result                 = PathParameterValidator.withPstrNumber("NotPstr")(sampleBlock)
      result.futureValue mustBe InternalServerError(Json.obj(
        "error"   -> "Invalid Pstr",
        "details" -> "NotPstr"
      ))
    }
  }
}
