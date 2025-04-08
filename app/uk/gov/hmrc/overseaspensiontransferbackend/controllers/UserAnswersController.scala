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

import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.models.UserAnswers
import uk.gov.hmrc.overseaspensiontransferbackend.services.CompileAndSubmitService
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserAnswersController @Inject() (
                                        cc: ControllerComponents,
                                        compileAndSubmitService: CompileAndSubmitService
                                      )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  /**
   * GET /user-answers/:id
   * Calls the service to retrieve user answers for the provided ID.
   * Returns 200 + JSON if found, 404 otherwise.
   */
  def getAnswers(id: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequest(request)

    compileAndSubmitService.getAnswers(id).map {
      case Some(a) => Ok(Json.toJson(a))
      case None    => NotFound
    }
  }

  /**
   * PUT /user-answers/:id
   * Validates incoming JSON as UserAnswers, then delegates to service to upsert.
   */
  def putAnswers(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequest(request)

    request.body.validate[UserAnswers] match {
      case JsSuccess(userAnswers, _) =>
        val updatedAnswers = userAnswers.copy(id = id)

        compileAndSubmitService.upsertAnswers(updatedAnswers).map { saved =>
          Ok(Json.toJson(saved))
        }

      case JsError(e) =>
        Future.successful(BadRequest(s"Invalid JSON for UserAnswers: $e"))
    }
  }
}
