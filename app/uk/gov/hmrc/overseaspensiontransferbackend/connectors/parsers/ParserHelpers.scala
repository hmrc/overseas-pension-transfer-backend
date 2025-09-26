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

package uk.gov.hmrc.overseaspensiontransferbackend.connectors.parsers

import play.api.http.Status._
import play.api.libs.json.Reads
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream._

object ParserHelpers {
  private[parsers] val MaxSnippet = 512

  /** Central response dispatcher: status → JSON shape → ADT */
  def handleResponse[A](resp: HttpResponse, successStatus: Int = OK)(implicit reads: Reads[A]): Either[DownstreamError, A] =
    resp.status match {
      case status if status == successStatus =>
        resp.json.validate[A]
          .asEither
          .left.map(_ => Unexpected(successStatus, resp.body.take(MaxSnippet)))

      case BAD_REQUEST =>
        Left(parseHipEnvelope(resp))

      case UNPROCESSABLE_ENTITY =>
        Left(
          resp.json.validate[EtmpValidationError]
            .asOpt
            .map(mapEtmp422)
            .getOrElse(Unexpected(UNPROCESSABLE_ENTITY, resp.body.take(MaxSnippet)))
        )

      case INTERNAL_SERVER_ERROR =>
        Left(parseHipEnvelope(resp))

      case SERVICE_UNAVAILABLE =>
        Left(parseHipEnvelope(resp))

      case UNAUTHORIZED =>
        Left(Unauthorized)

      case FORBIDDEN =>
        Left(Forbidden)

      case NOT_FOUND =>
        Left(NotFound)

      case UNSUPPORTED_MEDIA_TYPE =>
        Left(UnsupportedMedia)

      case other =>
        Left(Unexpected(other, resp.body.take(MaxSnippet)))
    }

  private def mapEtmp422(ev: EtmpValidationError): DownstreamError =
    ev.code match {
      case "183" => NoTransfersFound
      case _     => ev
    }

  /** HIP envelopes (400/500/503): try error-object, then failures-array; trim long strings */
  private def parseHipEnvelope(resp: HttpResponse): DownstreamError =
    resp.json.validate[HipBadRequest].asOpt
      .map(hb => hb.copy(message = hb.message.take(MaxSnippet)))
      .orElse {
        resp.json.validate[HipOriginFailures].asOpt.map { hf =>
          hf.copy(failures =
            hf.failures.map(f =>
              f.copy(reason = f.reason.take(MaxSnippet))
            )
          )
        }
      }
      .getOrElse(Unexpected(resp.status, resp.body.take(MaxSnippet)))
}
