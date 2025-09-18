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
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models.PstrNumber
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.GetAllTransfersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.AllTransfersResponse
import uk.gov.hmrc.overseaspensiontransferbackend.services.SubmissionService
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.Clock
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetAllTransfersController @Inject() (
    cc: ControllerComponents,
    submissionService: SubmissionService,
    clock: Clock
  )(implicit ec: ExecutionContext,
    appConfig: AppConfig
  ) extends AbstractController(cc) {

  def getAllTransfers(pstrNumber: String): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    val maybePstr                  = PstrNumber.from(pstrNumber)

    maybePstr match {
      case Right(validatedPstr) =>
        submissionService.getAllTransfers(validatedPstr).flatMap {
          case Right(AllTransfersResponse(maybeTransfers)) =>
            maybeTransfers match {
              case Some(transfers) =>
                val dto = GetAllTransfersDTO.from(validatedPstr, transfers)(clock)
                Future.successful(Ok(Json.toJson(dto)))
              case None            => Future.successful(NotFound)
            }
          case Left(_)                                     => ???
        }
      case Left(m)              => Future.successful(BadRequest(m))
    }
  }
}
