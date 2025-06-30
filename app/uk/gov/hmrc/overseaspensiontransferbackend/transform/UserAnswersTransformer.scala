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

  def toSaved(dto: UserAnswersDTO): Either[JsError, SavedUserAnswers] = {
    applyCleanseTransforms(dto.data).flatMap { cleansed =>
      cleansed.validate[AnswersData].asEither.left.map(JsError.apply).map { parsed =>
        SavedUserAnswers(dto.referenceId, parsed, dto.lastUpdated)
      }
    }
  }

  def fromSaved(saved: SavedUserAnswers): Either[JsError, UserAnswersDTO] = {
    val baseJson = Json.toJsObject(saved.data)
    applyEnrichTransforms(baseJson).map { enriched =>
      UserAnswersDTO(saved.referenceId, enriched, saved.lastUpdated)
    }
  }

  def applyCleanseTransforms(json: JsObject): Either[JsError, JsObject] = {
    val transformers: Seq[JsObject => Either[JsError, JsObject]] = Seq(
      MemberDetailsTransformer.cleanse
      // Add others here
    )

    transformers.foldLeft(Right(json): Either[JsError, JsObject]) {
      case (Right(current), transform) => transform(current)
      case (left @ Left(_), _)         => left
    }
  }

  def applyEnrichTransforms(json: JsObject): Either[JsError, JsObject] = {
    val transformers: Seq[JsObject => Either[JsError, JsObject]] = Seq(
      MemberDetailsTransformer.enrich
      // Add others here
    )

    transformers.foldLeft(Right(json): Either[JsError, JsObject]) {
      case (Right(current), transform) => transform(current)
      case (left @ Left(_), _)         => left
    }
  }
}
