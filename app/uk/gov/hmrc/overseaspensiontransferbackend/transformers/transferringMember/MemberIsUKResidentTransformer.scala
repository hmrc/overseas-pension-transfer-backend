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

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{BooleanTransformers, Transformer, TransformerUtils}
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

class MemberIsUKResidentTransformer extends Transformer with JsonHelpers with BooleanTransformers {

  private val jsonKey = "memUkResident"

  override def construct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      movePath(
        from = JsPath \ "memberDetails" \ jsonKey,
        to   = JsPath \ "transferringMember" \ "memberDetails" \ "memberResidencyDetails" \ jsonKey,
        _: JsObject
      ),
      constructBool(
        JsPath \ "transferringMember" \ "memberDetails" \ "memberResidencyDetails" \ jsonKey
      )
    )
    TransformerUtils.applyPipeline(json, steps)(identity)
  }

  override def deconstruct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      deconstructBool(
        JsPath \ "transferringMember" \ "memberDetails" \ "memberResidencyDetails" \ jsonKey
      ),
      movePath(
        from = JsPath \ "transferringMember" \ "memberDetails" \ "memberResidencyDetails" \ jsonKey,
        to   = JsPath \ "memberDetails" \ jsonKey,
        _: JsObject
      )
    )

    TransformerUtils.applyPipeline(json, steps)(identity)
  }
}
