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
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.Transformer

class WrapTransferringMemberTransformer extends Transformer {

  override def applyCleanseTransforms(json: JsObject): Either[JsError, JsObject] = {
    if ((json \ "transferringMember").isDefined) {
      Right(json)
    } else {
      Right(Json.obj("transferringMember" -> json))
    }
  }

  override def applyEnrichTransforms(json: JsObject): Either[JsError, JsObject] = {
    (json \ "transferringMember").asOpt[JsObject] match {
      case Some(inner) => Right(inner)
      case None        => Right(json)
    }
  }
}
