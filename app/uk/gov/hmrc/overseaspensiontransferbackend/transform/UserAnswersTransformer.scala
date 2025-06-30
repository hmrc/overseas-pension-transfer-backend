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

package uk.gov.hmrc.overseaspensiontransferbackend.transform

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.models._
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO

object UserAnswersTransformer {

  private val transformers: Seq[JsonTransformerStep] = Seq(
    MemberDetailsTransformer
  )

  def applyCleanseTransforms(json: JsObject): Either[JsError, JsObject] = {
    applyPipeline(json)(_.cleanse)
  }

  def applyEnrichTransforms(json: JsObject): Either[JsError, JsObject] = {
    applyPipeline(json)(_.enrich)
  }

  private def applyPipeline(json: JsObject)(step: JsonTransformerStep => JsObject => Either[JsError, JsObject]): Either[JsError, JsObject] = {
    transformers.foldLeft(Right(json): Either[JsError, JsObject]) {
      case (Right(current), transformer) => step(transformer)(current)
      case (err @ Left(_), _)            => err
    }
  }
}
