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

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsError, JsObject, Json}

case class TransferringMemberTransformer() extends Transformer {

  override def construct(input: JsObject): Either[JsError, JsObject] = {
    Right(Json.obj("transferringMember" -> input))
  }

  def deconstruct(constructed: JsObject): Either[JsError, JsObject] = {
    val lookup = (constructed \ "transferringMember" \ "memberDetails").validateOpt[JsObject].get
    lookup match {
      case Some(res) => Right(Json.obj("memberDetails" -> res))
      case _         => Left(JsError("memberDetails does not exist"))
    }
  }
}
