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
import uk.gov.hmrc.overseaspensiontransferbackend.models.UserAnswers

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class UserAnswersController @Inject() (
                                        cc: ControllerComponents
                                      )(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  private var store: Map[String, UserAnswers] = Map.empty

  def getAnswers(id: String): Action[AnyContent] = Action.async {
    val answers = store.get(id)
    answers match {
      case Some(a) => Future.successful(Ok(Json.toJson(a)))
      case None => Future.successful(NotFound)
    }
  }

  def putAnswers(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[UserAnswers] match {
      case JsSuccess(userAnswers, _) =>
        store = store.updated(id, userAnswers.copy(id = id))
        Future.successful(Ok(Json.toJson(store(id))))
      case JsError(errors) =>
        Future.successful(BadRequest("Invalid JSON"))
    }
  }
}
