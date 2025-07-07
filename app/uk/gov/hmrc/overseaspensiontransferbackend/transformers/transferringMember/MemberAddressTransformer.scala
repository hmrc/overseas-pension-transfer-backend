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
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{AddressTransformers, Transformer, TransformerUtils}
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

class MemberAddressTransformer extends Transformer with AddressTransformers with JsonHelpers {

  private val jsonKey = "principalResAddDetails"

  override def construct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      movePath(
        from = JsPath \ "memberDetails" \ jsonKey,
        to   = JsPath \ "transferringMember" \ "memberDetails" \ jsonKey,
        _: JsObject
      ),
      constructAddressAt(
        JsPath \ "transferringMember" \ "memberDetails" \ jsonKey
      )
    )

    TransformerUtils.applyPipeline(json, steps)(identity)
  }

  override def deconstruct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      deconstructAddressAt(
        JsPath \ "transferringMember" \ "memberDetails" \ jsonKey
      ),
      movePath(
        from = JsPath \ "transferringMember" \ "memberDetails" \ jsonKey,
        to   = JsPath \ "memberDetails" \ jsonKey,
        _: JsObject
      )
    )

    TransformerUtils.applyPipeline(json, steps)(identity)
  }
}
