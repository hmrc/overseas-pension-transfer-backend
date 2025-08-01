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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.steps.TransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

trait EnumTransformerStep extends JsonHelpers {

  def constructEnum[A](path: JsPath, func: A => JsValue)(implicit reads: Reads[A]): TransformerStep = json =>
    path.asSingleJson(json).validate[A] match {
      case JsSuccess(value, _) => {
        setPath(path, func(value), json)
      }
      case JsError(_)          => {
        Right(json)
      }
    }

  def deconstructEnum[A](path: JsPath, func: String => A): TransformerStep = json =>
    path.asSingleJson(json).validate[String] match {
      case JsSuccess(value, _) => setPath(path, JsString(func(value).toString), json)
      case JsError(_)          => Right(json)
    }
}
