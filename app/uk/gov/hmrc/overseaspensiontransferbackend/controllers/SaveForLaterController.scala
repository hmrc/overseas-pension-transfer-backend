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

import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO.fromSavedUserAnswers
import uk.gov.hmrc.overseaspensiontransferbackend.services.SaveForLaterService
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SaveForLaterController @Inject() (
    cc: ControllerComponents,
    saveForLaterService: SaveForLaterService
  )(implicit ec: ExecutionContext
  ) extends AbstractController(cc) {

  def getAnswers(referenceID: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequest(request)

    saveForLaterService.getAnswers(referenceID).map {
      case Some(a) => Ok(Json.toJson(a))
      case None    => NotFound
    }
  }

  def saveAnswers(referenceId: String): Action[UserAnswersDTO] =
    Action.async(parse.json[UserAnswersDTO]) { request =>
      implicit val hc: HeaderCarrier =
        HeaderCarrierConverter.fromRequest(request)
      val userAnswersDTO             = request.body.copy(referenceId = referenceId)
      saveForLaterService.saveAnswers(userAnswersDTO).map {
        case Some(savedAnswers) => Created(Json.toJson(fromSavedUserAnswers(savedAnswers)))
        case None               => InternalServerError
      }
    }
}
