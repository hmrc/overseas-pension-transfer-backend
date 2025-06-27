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
import play.api.libs.json.{JsError, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.models.SavedUserAnswers
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
  def saveAnswers(answers: UserAnswersDTO)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, UserAnswersDTO]]
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

  override def saveAnswers(dto: UserAnswersDTO)(implicit hc: HeaderCarrier): Future[Either[SaveForLaterError, UserAnswersDTO]] = {
    UserAnswersTransformer.toSaved(dto) match {
      case Left(JsError(errors)) =>
        Future.successful(Left(TransformationError(Json.prettyPrint(JsError.toJson(errors)))))

      case Right(savedUserAnswers) =>
        repository.set(savedUserAnswers).map {
          case true  => Right(dto)
          case false => Left(SaveFailed)
        }
    }
  }
}
