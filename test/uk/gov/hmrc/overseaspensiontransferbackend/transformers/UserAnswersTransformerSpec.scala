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
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import play.api.libs.json._

class UserAnswersTransformerSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  "UserAnswersTransformer" - {

    "should apply all registered cleanse transformers in order" in {
      val input        = Json.obj("key" -> "value")
      val intermediate = Json.obj("step1" -> "done")
      val finalOutput  = Json.obj("step2" -> "done")

      val transformer1 = mock[Transformer]
      val transformer2 = mock[Transformer]

      val sut = new UserAnswersTransformer(Seq(transformer1, transformer2))

      when(transformer1.construct(any())).thenReturn(Right(intermediate))
      when(transformer2.construct(intermediate)).thenReturn(Right(finalOutput))

      val result = sut.construct(input)

      result mustBe Right(finalOutput)
      verify(transformer1).construct(input)
      verify(transformer2).construct(intermediate)
    }

    "should short-circuit cleanse transforms if a transformer fails" in {
      val input = Json.obj("start" -> "bad")
      val error = JsError("fail!")

      val transformer1 = mock[Transformer]
      val transformer2 = mock[Transformer]

      val sut = new UserAnswersTransformer(Seq(transformer1, transformer2))

      when(transformer1.construct(input)).thenReturn(Left(error))

      val result = sut.construct(input)

      result mustBe Left(error)
      verify(transformer1).construct(input)
      verifyNoInteractions(transformer2)
    }
  }
}
