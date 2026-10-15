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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferDetails

import play.api.libs.json.{JsArray, JsError, JsObject, JsPath, JsString}
import uk.gov.hmrc.overseaspensiontransferbackend.models.ApplicableExclusion
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.EnumTransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.Transformer

class ReasonNoOverseasTransferTransformer extends Transformer with EnumTransformerStep {

  override val externalPath: JsPath = JsPath \ "transferDetails" \ "reasonNoOverseasTransfer"
  override val internalPath: JsPath = externalPath

  override def construct(input: JsObject): Either[JsError, JsObject] = {
    val enumConversion: Seq[ApplicableExclusion] => JsArray = applicableExclusions =>
      JsArray(
        applicableExclusions.map {
          applicableExclusion =>
            JsString(applicableExclusion.downstreamValue)
        }
      )
    applyPipeline(input, Seq(constructEnum[Seq[ApplicableExclusion]](externalPath, enumConversion)))(identity)
  }

  override def deconstruct(input: JsObject): Either[JsError, JsObject] = {
    val enumConversion: Seq[ApplicableExclusion] => JsArray = applicableExclusions =>
      JsArray(
        applicableExclusions.map {
          applicableExclusion =>
            JsString(applicableExclusion.toReasonNoOverseasTransferString)
        }
      )
    applyPipeline(input, Seq(constructEnum[Seq[ApplicableExclusion]](externalPath, enumConversion)))(identity)
  }
}
