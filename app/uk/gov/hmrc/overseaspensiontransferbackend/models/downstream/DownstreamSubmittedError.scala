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

sealed trait DownstreamSubmittedError

// ---------- 400/500: HIP responseSystemErrorType ----------
/** Example: { "origin": "HoD|HIP", "response": { "error": { "code": "400|500", "logID": "...", "message": "..." } } }
  */
final case class HipBadRequest(origin: String, code: String, message: String, logId: Option[String]) extends DownstreamSubmittedError

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
final case class HipOriginFailures(origin: String, failures: List[HipOriginFailures.Failure]) extends DownstreamSubmittedError

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
final case class EtmpValidationSubmittedError(processingDate: String, code: String, text: String) extends DownstreamSubmittedError

object EtmpValidationSubmittedError {
  private case class Errors(processingDate: String, code: String, text: String)

  implicit private val errorsReads: Reads[Errors] =
    (
      (__ \ "errors" \ "processingDate").read[String] and
        (__ \ "errors" \ "code").read[String] and
        (__ \ "errors" \ "text").read[String]
    )(Errors.apply _)

  implicit val reads: Reads[EtmpValidationSubmittedError] =
    implicitly[Reads[Errors]].map(e => EtmpValidationSubmittedError(e.processingDate, e.code, e.text))
}

// -------- Other status families --------
case object Unauthorized          extends DownstreamSubmittedError // 401
case object Forbidden             extends DownstreamSubmittedError // 403
case object NotFound              extends DownstreamSubmittedError // 404
case object UnsupportedMedia      extends DownstreamSubmittedError // 415
case object ServerSubmittedError$ extends DownstreamSubmittedError // 500 (fallback if body doesn’t match HIP shapes)
case object ServiceUnavailable    extends DownstreamSubmittedError // 503

// ---------- Fallback ----------
final case class Unexpected(status: Int, bodySnippet: String) extends DownstreamSubmittedError
