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

import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.models.{Pstr, QtStatus}
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.GetSpecificTransferDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.{TransferNotFound, TransferRetrievalError}
import uk.gov.hmrc.overseaspensiontransferbackend.services.SubmissionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.ExecutionContext

class GetTransferDataController @Inject() (cc: ControllerComponents, submissionService: SubmissionService)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def getTransfer(
      referenceId: String,
      pstr: String,
      qtStatus: String,
      versionNumber: Option[String]
    ): Action[AnyContent] =
    Action.async {
      implicit request =>
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

        val transferType: Either[TransferRetrievalError, GetSpecificTransferDTO] =
          GetSpecificTransferDTO.apply(referenceId, Pstr(pstr), QtStatus(qtStatus), versionNumber)

        submissionService.getTransfer(transferType) map {
          case Right(value)              => Ok(Json.toJson(value))
          case Left(TransferNotFound(_)) => NotFound
          case Left(_)                   => InternalServerError
        }
    }

}
