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
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.overseaspensiontransferbackend.controllers.actions.IdentifierAction
import uk.gov.hmrc.overseaspensiontransferbackend.models.QtStatus
import uk.gov.hmrc.overseaspensiontransferbackend.services.TransferService
import uk.gov.hmrc.overseaspensiontransferbackend.validators.PathParameterValidator.isPstrNumber
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

class GetTransferDataController @Inject() (
    cc: ControllerComponents,
    transferService: TransferService,
    identify: IdentifierAction
  )(implicit ec: ExecutionContext
  ) extends BackendController(cc) with Logging {

  def getTransfer(
      referenceId: String,
      pstr: String,
      qtStatus: String,
      versionNumber: Option[String]
    ): Action[AnyContent] =
    identify.async {
      implicit request =>
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
        if (isPstrNumber(pstr)) {
          transferService.getTransfer(referenceId, pstr, QtStatus(qtStatus), versionNumber) map {
            case Right(value)                                                => Ok(Json.toJson(value))
            case Left(x: UpstreamErrorResponse) if x.statusCode == NOT_FOUND => NotFound
            case Left(e)                                                     =>
              InternalServerError(Json.obj(
                "error"   -> "Internal server error",
                "details" -> e.toString
              ))
          }
        } else {
          logger.error("PstrNumber has an invalid format. PstrNumber: " + pstr)
          Future.successful(InternalServerError(Json.obj(
            "error"   -> "Invalid Pstr",
            "details" -> pstr
          )))
        }
    }

}
