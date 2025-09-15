package uk.gov.hmrc.overseaspensiontransferbackend.controllers

import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.services.SubmissionService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.ExecutionContext

class GetTransferDataController @Inject()(cc: ControllerComponents, submissionService: SubmissionService)(implicit ec: ExecutionContext) extends BackendController(cc) {

  def getTransfer(pstr: String, formBundleNumber: Option[String] = None, qtRef: Option[String] = None, version: Option[String] = None) = Action.async {
    implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

      submissionService.getTransfer(pstr, formBundleNumber, qtRef, version) map {
        case Right(value) => Ok(Json.toJson(value))
        case Left(error) => InternalServerError
      }

  }

}
