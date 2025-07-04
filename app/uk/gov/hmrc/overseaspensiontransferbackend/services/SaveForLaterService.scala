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

package uk.gov.hmrc.overseaspensiontransferbackend.services

import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.models.{AnswersData, SavedUserAnswers}
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.UserAnswersTransformer

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

sealed trait SaveForLaterError

object SaveForLaterError {
  final case class TransformationError(msg: String) extends SaveForLaterError
  final case object NotFound                        extends SaveForLaterError
  final case object SaveFailed                      extends SaveForLaterError
}

trait SaveForLaterService {
  def getAnswers(id: String)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, UserAnswersDTO]]
  def saveAnswer(answers: UserAnswersDTO)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, Unit]]
}

@Singleton
class SaveForLaterServiceImpl @Inject() (
    repository: SaveForLaterRepository,
    userAnswersTransformer: UserAnswersTransformer
  )(implicit ec: ExecutionContext
  ) extends SaveForLaterService with Logging {

  override def getAnswers(id: String)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, UserAnswersDTO]] = {
    repository.get(id).map {
      case Some(saved) =>
        enrich(Json.toJsObject(saved.data)) match {
          case Right(enriched) =>
            Right(UserAnswersDTO(saved.referenceId, enriched, saved.lastUpdated))

          case Left(error) =>
            Left(error)
        }

      case None =>
        Left(SaveForLaterError.NotFound)
    }
  }

  override def saveAnswer(dto: UserAnswersDTO)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, Unit]] = {
    cleanse(dto.data) match {
      case Left(err) => Future.successful(Left(err))

      case Right(transformedInput) =>
        repository.get(dto.referenceId).flatMap { maybeExisting =>
          val mergedJson = mergeWithExisting(maybeExisting, transformedInput)
          validate(mergedJson) match {
            case Left(err) => Future.successful(Left(err))

            case Right(validated) =>
              println(transformedInput)
              println(validated)
              val saved = SavedUserAnswers(dto.referenceId, validated, dto.lastUpdated)
              repository.set(saved).map {
                case true  => Right()
                case false => Left(SaveForLaterError.SaveFailed)
              }
          }
        }
    }
  }

  private def cleanse(json: JsObject): Either[SaveForLaterError, JsObject] = {
    userAnswersTransformer.applyCleanseTransforms(json).left.map(err =>
      SaveForLaterError.TransformationError(Json.prettyPrint(JsError.toJson(err)))
    )
  }

  private def enrich(json: JsObject): Either[SaveForLaterError, JsObject] = {
    userAnswersTransformer.applyEnrichTransforms(json).left.map(err =>
      SaveForLaterError.TransformationError(Json.prettyPrint(JsError.toJson(err)))
    )
  }

  private def validate(json: JsObject): Either[SaveForLaterError, AnswersData] = {
    json.validate[AnswersData] match {
      case JsSuccess(data, _) => Right(data)
      case JsError(err)       => Left(SaveForLaterError.TransformationError(Json.prettyPrint(JsError.toJson(err))))
    }
  }

  private def mergeWithExisting(existing: Option[SavedUserAnswers], update: JsObject): JsObject = {
    existing match {
      case Some(existingData) => Json.toJsObject(existingData.data).deepMerge(update)
      case None               => update
    }
  }
}
