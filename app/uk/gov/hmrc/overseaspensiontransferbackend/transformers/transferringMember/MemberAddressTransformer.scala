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
import uk.gov.hmrc.overseaspensiontransferbackend.models.external.{MemberDetailsExternalField, TaskField}
import uk.gov.hmrc.overseaspensiontransferbackend.models.internal.{AnswersDataField, TransferringMemberField}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.steps._
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.AddressTransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{PathAwareTransformer, TransformerUtils}
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

class MemberAddressTransformer extends PathAwareTransformer with AddressTransformerStep with JsonHelpers {

  private val jsonKey               = MemberDetailsExternalField.PrincipalResAddDetails.toString
  override val externalPath: JsPath = JsPath \ TaskField.MemberDetails.toString \ jsonKey
  override val internalPath: JsPath = JsPath \ AnswersDataField.TransferringMember.toString \ TransferringMemberField.MemberDetails.toString \ jsonKey
  private val nestedKey             = "addressDetails"

  override def construct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      moveStep(
        from = externalPath,
        to   = internalPath
      ),
      constructAddressAt(
        path = internalPath,
        nestedKey
      )
    )

    TransformerUtils.applyPipeline(json, steps)(identity)
  }

  override def deconstruct(json: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      deconstructAddressAt(
        internalPath \
          nestedKey
      ),
      deconstructToExternal
    )

    TransformerUtils.applyPipeline(json, steps)(identity)
  }

  private val deconstructToExternal: TransformerStep = { json =>
    for {
      withAddress <- moveStep(internalPath \ nestedKey, externalPath)(json)
      withPoBox   <- moveStep(internalPath \ "poBoxNumber", externalPath \ "poBoxNumber")(withAddress)
    } yield withPoBox
  }
}
