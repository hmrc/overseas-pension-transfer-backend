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
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.controllers.actions.IdentifierAction
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.SubmissionDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{SubmissionFailed, SubmissionTransformationError, TransferId}
import uk.gov.hmrc.overseaspensiontransferbackend.services.TransferService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SubmissionController @Inject() (
    cc: ControllerComponents,
    transferService: TransferService,
    identify: IdentifierAction
  )(implicit ec: ExecutionContext
  ) extends BackendController(cc) {

  def submitTransfer(referenceId: TransferId): Action[SubmissionDTO] =
    identify.async(parse.json[SubmissionDTO]) { request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

      val normalised = request.body.normalise(referenceId)

      transferService.submitTransfer(normalised).map {
        case Right(submissionResponse)                =>
          Ok(Json.toJson(submissionResponse))
        case Left(SubmissionTransformationError(msg)) =>
          BadRequest(Json.obj(
            "error"   -> "Transformation failed",
            "details" -> msg
          ))
        case Left(SubmissionFailed)                   =>
          InternalServerError(Json.obj("error" -> "Failed to submit"))
        case Left(other)                              =>
          InternalServerError(Json.obj("error" -> s"Unexpected error: $other"))
      }
    }
}
