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

import play.api.libs.json._

class TransferringMemberTransformer(
    nestedTransformers: Seq[Transformer] = Seq.empty
  ) extends Transformer {

  override def construct(input: JsObject): Either[JsError, JsObject] = {
    val memberDetailsOpt = (input \ "memberDetails").asOpt[JsObject]

    memberDetailsOpt match {
      case Some(memberDetailsJson) =>
        // Apply nested transformers to memberDetails
        val transformedMemberDetails = TransformerUtils.applyPipeline(memberDetailsJson, nestedTransformers)(_.construct)

        transformedMemberDetails.map { transformed =>
          Json.obj("transferringMember" -> Json.obj("memberDetails" -> transformed))
        }

      case None =>
        Left(JsError("memberDetails not found in input JSON"))
    }
  }

  override def deconstruct(input: JsObject): Either[JsError, JsObject] = {
    val maybeMemberDetails = (input \ "transferringMember" \ "memberDetails").asOpt[JsObject]

    maybeMemberDetails match {
      case Some(memberDetails) =>
        val transformedBack = TransformerUtils.applyPipeline(memberDetails, nestedTransformers)(_.deconstruct)

        transformedBack.map { unwrapped =>
          Json.obj("memberDetails" -> unwrapped)
        }

      case None =>
        Left(JsError("transferringMember.memberDetails not found in input JSON"))
    }
  }
}
