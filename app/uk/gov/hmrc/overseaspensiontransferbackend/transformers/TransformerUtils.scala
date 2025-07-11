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

object TransformerUtils {

  def applyPipeline[T](
      json: JsObject,
      steps: Seq[T]
    )(
      stepFn: T => JsObject => Either[JsError, JsObject]
    ): Either[JsError, JsObject] = {
    steps.foldLeft(Right(json): Either[JsError, JsObject]) {
      case (Right(current), transformer) => stepFn(transformer)(current)
      case (err @ Left(_), _)            => err
    }
  }

}
