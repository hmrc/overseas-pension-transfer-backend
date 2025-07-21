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
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.steps._
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.AddressTransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{PathAwareTransformer, TransformerUtils}
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

class MemberLastUKAddressTransformer extends PathAwareTransformer with AddressTransformerStep with JsonHelpers {

  val jsonKey                       = "lastPrincipalAddDetails"
  override val externalPath: JsPath = JsPath \ "memberDetails" \ jsonKey
  override val internalPath: JsPath = JsPath \ "transferringMember" \ "memberDetails" \ "memberResidencyDetails" \ jsonKey
  private val nestedKey             = "addressDetails"

  override def construct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      moveStep(
        from = externalPath,
        to   = internalPath
      ),
      constructAddressAt(
        internalPath,
        nestedKey
      )
    )

    TransformerUtils.applyPipeline(json, steps)(identity)
  }

  override def deconstruct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      deconstructAddressAt(
        internalPath \ nestedKey
      ),
      moveStep(
        from = internalPath \ nestedKey,
        to   = externalPath
      )
    )

    TransformerUtils.applyPipeline(json, steps)(identity)
  }
}
