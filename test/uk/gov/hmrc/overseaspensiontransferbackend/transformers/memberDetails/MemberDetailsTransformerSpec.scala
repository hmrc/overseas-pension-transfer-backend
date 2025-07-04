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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.TransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferringMember.memberDetails.MemberDetailsTransformer

class MemberDetailsTransformerSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  "MemberDetailsTransformer" - {

    "should apply all cleanse steps in order" in {
      val input        = Json.obj("input" -> "value")
      val intermediate = Json.obj("step1" -> "done")
      val finalOutput  = Json.obj("step2" -> "done")

      val step1 = mock[TransformerStep]
      val step2 = mock[TransformerStep]

      when(step1.cleanse(any())).thenReturn(Right(intermediate))
      when(step2.cleanse(intermediate)).thenReturn(Right(finalOutput))

      val transformer = new MemberDetailsTransformer(Seq(step1, step2))

      val result = transformer.applyCleanseTransforms(input)

      result mustBe Right(finalOutput)
      verify(step1).cleanse(input)
      verify(step2).cleanse(intermediate)
    }

    "should short-circuit on first cleanse step failure" in {
      val input = Json.obj("bad" -> "data")
      val error = JsError("failure")

      val step1 = mock[TransformerStep]
      val step2 = mock[TransformerStep]

      when(step1.cleanse(input)).thenReturn(Left(error))

      val transformer = new MemberDetailsTransformer(Seq(step1, step2))

      val result = transformer.applyCleanseTransforms(input)

      result mustBe Left(error)
      verify(step1).cleanse(input)
      verifyNoInteractions(step2)
    }

    "should apply all enrich steps in order" in {
      val input        = Json.obj("x" -> "1")
      val intermediate = Json.obj("x" -> "2")
      val finalOutput  = Json.obj("x" -> "3")

      val step1 = mock[TransformerStep]
      val step2 = mock[TransformerStep]

      when(step1.enrich(any())).thenReturn(Right(intermediate))
      when(step2.enrich(intermediate)).thenReturn(Right(finalOutput))

      val transformer = new MemberDetailsTransformer(Seq(step1, step2))

      val result = transformer.applyEnrichTransforms(input)

      result mustBe Right(finalOutput)
      verify(step1).enrich(input)
      verify(step2).enrich(intermediate)
    }
  }
}
