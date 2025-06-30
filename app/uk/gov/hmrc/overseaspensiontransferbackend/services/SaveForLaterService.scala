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
import uk.gov.hmrc.overseaspensiontransferbackend.transform.UserAnswersTransformer

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
  def saveAnswer(answers: UserAnswersDTO)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, UserAnswersDTO]]
}

@Singleton
class SaveForLaterServiceImpl @Inject() (
    repository: SaveForLaterRepository
  )(implicit ec: ExecutionContext
  ) extends SaveForLaterService with Logging {

  import SaveForLaterError._

  override def getAnswers(id: String)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, UserAnswersDTO]] =
    repository.get(id).map {
      case Some(saved) =>
        UserAnswersTransformer.fromSaved(saved) match {
          case Right(dto) => Right(dto)
          case Left(err)  => Left(TransformationError(Json.stringify(JsError.toJson(err))))
        }

      case None =>
        Left(NotFound)
    }

  override def saveAnswer(dto: UserAnswersDTO)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, UserAnswersDTO]] = {
    UserAnswersTransformer.applyCleanseTransforms(dto.data) match {
      case Left(JsError(errors)) =>
        Future.successful(Left(TransformationError(Json.prettyPrint(JsError.toJson(errors)))))

      case Right(transformedInput) =>
        repository.get(dto.referenceId).flatMap {

          case Some(existing) =>
            val existingJson = Json.toJsObject(existing.data)
            val mergedJson   = existingJson.deepMerge(transformedInput)

            UserAnswersTransformer.applyEnrichTransforms(transformedInput) match {
              case Left(JsError(errors)) =>
                Future.successful(Left(TransformationError(Json.prettyPrint(JsError.toJson(errors)))))

              case Right(enrichedPartial) =>
                mergedJson.validate[AnswersData] match {
                  case JsSuccess(mergedAnswersData, _) =>
                    val saved = SavedUserAnswers(dto.referenceId, mergedAnswersData, dto.lastUpdated)
                    repository.set(saved).map {
                      case true  => Right(dto.copy(data = enrichedPartial))
                      case false => Left(SaveFailed)
                    }

                  case JsError(errors) =>
                    Future.successful(Left(TransformationError(Json.prettyPrint(JsError.toJson(errors)))))
                }
            }
          case None =>
            UserAnswersTransformer.applyEnrichTransforms(transformedInput) match {
              case Left(JsError(errors)) =>
                Future.successful(Left(TransformationError(Json.prettyPrint(JsError.toJson(errors)))))

              case Right(enrichedPartial) =>
                transformedInput.validate[AnswersData] match {
                  case JsSuccess(data, _) =>
                    val saved = SavedUserAnswers(dto.referenceId, data, dto.lastUpdated)
                    repository.set(saved).map {
                      case true  => Right(dto.copy(data = enrichedPartial))
                      case false => Left(SaveFailed)
                    }
                  case JsError(errors) =>
                    Future.successful(Left(TransformationError(Json.prettyPrint(JsError.toJson(errors)))))
                }
            }

        }
    }
  }
}
