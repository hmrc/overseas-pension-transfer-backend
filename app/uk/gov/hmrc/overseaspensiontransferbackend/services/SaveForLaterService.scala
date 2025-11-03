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

import org.apache.pekko.Done
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{QtNumber, TransferId, TransferNumber}
import uk.gov.hmrc.overseaspensiontransferbackend.models.{AnswersData, Compiled, ReportDetails, SavedUserAnswers, Submitted}
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.UserAnswersTransformer
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

sealed trait SaveForLaterError

object SaveForLaterError {
  final case class TransformationError(msg: String) extends SaveForLaterError
  final case object NotFound                        extends SaveForLaterError
  final case object SaveFailed                      extends SaveForLaterError
  final case object DeleteFailed                    extends SaveForLaterError
}

trait SaveForLaterService {
  def getAnswers(id: TransferId)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, UserAnswersDTO]]
  def saveAnswer(answers: UserAnswersDTO)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, Unit]]
  def deleteAnswers(id: TransferId)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, Done]]
}

@Singleton
class SaveForLaterServiceImpl @Inject() (
    repository: SaveForLaterRepository,
    userAnswersTransformer: UserAnswersTransformer
  )(implicit ec: ExecutionContext
  ) extends SaveForLaterService with Logging with JsonHelpers {

  override def getAnswers(id: TransferId)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, UserAnswersDTO]] = {
    repository.get(id.value).map {
      case Some(saved) =>
        deconstructSavedAnswers(Json.toJsObject(saved.data)) match {
          case Right(deconstructed) =>
            Right(UserAnswersDTO(saved.transferId, saved.pstr, deconstructed, saved.lastUpdated))

          case Left(error) =>
            Left(error)
        }

      case None =>
        Left(SaveForLaterError.NotFound)
    }
  }

  override def saveAnswer(dto: UserAnswersDTO)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, Unit]] = {
    logger.info(s"Before construct: ${Json.prettyPrint(Json.toJson(dto.data))}")
    constructSavedAnswers(dto.data) match {
      case Left(err)               =>
        Future.successful(Left(err))
      case Right(transformedInput) =>
        logger.info(s"After construct: ${Json.prettyPrint(Json.toJson(transformedInput))}")
        repository.get(dto.transferId.value).flatMap { maybeExisting =>
          val mergedJson = mergeWithExisting(maybeExisting, transformedInput)
          validate(mergedJson) match {
            case Left(err) => Future.successful(Left(err))

            case Right(validated) =>
              val saved = SavedUserAnswers(dto.transferId, dto.pstr, validated, dto.lastUpdated)

              repository.set(saved).map {
                case true  => Right(())
                case false => Left(SaveForLaterError.SaveFailed)
              }
          }
        }
    }
  }

  private def constructSavedAnswers(json: JsObject): Either[SaveForLaterError, JsObject] = {
    userAnswersTransformer.construct(json).left.map { err =>
      val formatErr = Json.prettyPrint(JsError.toJson(err))
      logger.warn(formatErr)
      SaveForLaterError.TransformationError(formatErr)
    }
  }

  private def deconstructSavedAnswers(json: JsObject): Either[SaveForLaterError, JsObject] = {
    userAnswersTransformer.deconstruct(json).left.map { err =>
      val formatErr = Json.prettyPrint(JsError.toJson(err))
      logger.warn(formatErr)
      SaveForLaterError.TransformationError(formatErr)
    }
  }

  // Note that this only validates if any of the json keys are malformed, it does not validate for
  // missing or unexpected json, I looked into doing that and the solution is overly complex and
  // cumbersome
  private def validate(json: JsObject): Either[SaveForLaterError, AnswersData] = {
    json.validate[AnswersData] match {
      case JsSuccess(data, _) => {
        Right(data)
      }
      case JsError(err)       =>
        val formatErr = Json.prettyPrint(JsError.toJson(err))
        logger.warn(formatErr)
        Left(SaveForLaterError.TransformationError(formatErr))
    }
  }

  private def mergeWithExisting(existing: Option[SavedUserAnswers], update: JsObject): JsObject = {
    existing match {
      case Some(existingData) =>
        val base: JsObject = Json.toJsObject(existingData.data)
        val res            = pruneAndMerge(base, update)
        res
      case None               =>
        update
    }
  }

  override def deleteAnswers(id: TransferId)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, Done]] =
    repository.clear(id.value) map {
      case true  => Right(Done)
      case false => Left(SaveForLaterError.DeleteFailed)
    }
}
