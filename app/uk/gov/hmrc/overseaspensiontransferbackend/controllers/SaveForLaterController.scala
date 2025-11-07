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

package uk.gov.hmrc.overseaspensiontransferbackend.controllers

import org.apache.pekko.Done
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.controllers.actions.IdentifierAction
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.TransferId
import uk.gov.hmrc.overseaspensiontransferbackend.services.{SaveForLaterError, SaveForLaterService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SaveForLaterController @Inject() (
    cc: ControllerComponents,
    identify: IdentifierAction,
    saveForLaterService: SaveForLaterService
  )(implicit ec: ExecutionContext
  ) extends BackendController(cc) {

  def getAnswers(referenceId: TransferId): Action[AnyContent] = identify.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    saveForLaterService.getAnswers(referenceId).map {
      case Right(saved)                                         =>
        Ok(Json.toJson(saved))
      case Left(SaveForLaterError.TransformationError(message)) =>
        InternalServerError(Json.obj("error" -> "Failed to transform saved data", "details" -> message))

      case Left(SaveForLaterError.NotFound) =>
        NotFound(Json.obj("error" -> "No saved answers found"))

      case Left(other) =>
        InternalServerError(Json.obj("error" -> s"Unexpected error: $other"))
    }
  }

  def saveAnswers: Action[UserAnswersDTO] =
    identify.async(parse.json[UserAnswersDTO]) { request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

      saveForLaterService.saveAnswer(request.body).map {
        case Right(_)                                         =>
          NoContent
        case Left(SaveForLaterError.TransformationError(msg)) =>
          BadRequest(Json.obj(
            "error"   -> "Transformation failed",
            "details" -> msg
          ))
        case Left(SaveForLaterError.SaveFailed)               =>
          InternalServerError(Json.obj("error" -> "Failed to save answers"))
        case Left(other)                                      =>
          InternalServerError(Json.obj("error" -> s"Unexpected error: $other"))
      }
    }

  def deleteAnswers(referenceId: TransferId): Action[AnyContent] = identify.async {
    implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

      saveForLaterService.deleteAnswers(referenceId).map {
        case Right(Done) => NoContent
        case Left(error) => InternalServerError(Json.obj("error" -> s"Error while performing delete: $error"))
      }
  }
}
