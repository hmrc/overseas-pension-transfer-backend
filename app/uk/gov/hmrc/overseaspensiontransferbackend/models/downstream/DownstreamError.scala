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

package uk.gov.hmrc.overseaspensiontransferbackend.models.downstream

import play.api.libs.json._
import play.api.libs.functional.syntax._

sealed trait DownstreamError {
  def log: String
}

// ---------- 400/500: HIP responseSystemErrorType ----------
/** Example: { "origin": "HoD|HIP", "response": { "error": { "code": "400|500", "logID": "...", "message": "..." } } }
  */
final case class HipBadRequest(origin: String, code: String, message: String, logId: Option[String])
    extends DownstreamError {

  override def log: String =
    s"HIP error $code origin=$origin logId=${logId.getOrElse("-")} msg='$message'"
}

object HipBadRequest {

  implicit val reads: Reads[HipBadRequest] = (
    (__ \ "origin").read[String] and
      (__ \ "response" \ "error" \ "code").read[String] and
      (__ \ "response" \ "error" \ "message").read[String] and
      (__ \ "response" \ "error" \ "logID").readNullable[String]
  )(HipBadRequest.apply _)
}

// ---------- 400/500: HIP-originResponse (failures array) ----------
/** { "origin": "HIP|HoD", "response": { "failures": [ { "type": "...", "reason": "..." }, ... ] } } */
final case class HipOriginFailures(origin: String, failures: List[HipOriginFailures.Failure])
    extends DownstreamError {

  override def log: String = {
    val sample = failures.take(3).map(f => s"${f.`type`}:${f.reason}").mkString("; ")
    s"HIP failures origin=$origin count=${failures.size} sample=[$sample]"
  }
}

object HipOriginFailures {
  case class Failure(`type`: String, reason: String)
  implicit val failureFormat: OFormat[Failure] = Json.format[Failure]

  implicit val reads: Reads[HipOriginFailures] =
    (
      (__ \ "origin").read[String] and
        (__ \ "response" \ "failures").read[List[Failure]]
    )(HipOriginFailures.apply _)
}

// ---------- 422: ETMP Business Validation ----------
/** { "errors": { "processingDate": "...", "code": "001|003|...", "text": "..." } } */
final case class EtmpValidationError(processingDate: String, code: String, text: String)
    extends DownstreamError {
  override def log: String = s"422 ETMP(code=$code, text='$text')"
}

object EtmpValidationError {
  private case class Errors(processingDate: String, code: String, text: String)

  implicit private val errorsReads: Reads[Errors] =
    (
      (__ \ "errors" \ "processingDate").read[String] and
        (__ \ "errors" \ "code").read[String] and
        (__ \ "errors" \ "text").read[String]
    )(Errors.apply _)

  implicit val reads: Reads[EtmpValidationError] =
    implicitly[Reads[Errors]].map(e => EtmpValidationError(e.processingDate, e.code, e.text))
}

// -------- Other status families --------
case object Unauthorized       extends DownstreamError { override def log: String = "401 Unauthorized" }
case object Forbidden          extends DownstreamError { override def log: String = "403 Forbidden" }
case object NotFound           extends DownstreamError { override def log: String = "404 NotFound" }
case object UnsupportedMedia   extends DownstreamError { override def log: String = "415 UnsupportedMedia" }
case object NoTransfersFound   extends DownstreamError { override def log: String = "422.183 NoTransfersFound" }
case object ServerError        extends DownstreamError { override def log: String = "500 InternalServerError" } // (fallback if body doesn’t match HIP shapes)
case object ServiceUnavailable extends DownstreamError { override def log: String = "503 ServiceUnavailable" }

// ---------- Fallback ----------
final case class Unexpected(status: Int, body: String) extends DownstreamError {
  override def log: String = s"$status Unexpected body='$body'"
}
