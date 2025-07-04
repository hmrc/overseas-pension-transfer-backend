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
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.{Transformer, TransformerUtils}

import javax.inject.{Inject, Singleton}

@Singleton
class TransferringMemberTransformer @Inject() (
    memberDetailsTransformer: Transformer,
    memberResidencyDetailsTransformer: Transformer
  ) extends Transformer {

  override def applyCleanseTransforms(json: JsObject): Either[JsError, JsObject] = {
    (json \ "transferringMember").asOpt[JsObject] match {
      case Some(tmJson) =>
        for {
          updatedMemberDetails <- applyNested(tmJson, "memberDetails", memberDetailsTransformer.applyCleanseTransforms)
          updatedResidency     <- applyNested(updatedMemberDetails, "memberResidencyDetails", memberResidencyDetailsTransformer.applyCleanseTransforms)
        } yield json + ("transferringMember" -> updatedResidency)

      case None => Right(json)
    }
  }

  override def applyEnrichTransforms(json: JsObject): Either[JsError, JsObject] = {
    (json \ "transferringMember").asOpt[JsObject] match {
      case Some(tmJson) =>
        for {
          updatedMemberDetails <- applyNested(tmJson, "memberDetails", memberDetailsTransformer.applyEnrichTransforms)
          updatedResidency     <- applyNested(updatedMemberDetails, "memberResidencyDetails", memberResidencyDetailsTransformer.applyEnrichTransforms)
        } yield json + ("transferringMember" -> updatedResidency)

      case None => Right(json)
    }
  }

  private def applyNested(parent: JsObject, key: String, f: JsObject => Either[JsError, JsObject]): Either[JsError, JsObject] = {
    (parent \ key).asOpt[JsObject] match {
      case Some(nested) =>
        f(nested).map { transformed =>
          parent + (key -> transformed)
        }
      case None         => Right(parent)
    }
  }
}
