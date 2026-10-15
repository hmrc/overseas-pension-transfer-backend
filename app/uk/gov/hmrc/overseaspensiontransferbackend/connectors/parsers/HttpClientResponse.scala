/*
 * Copyright 2026 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.Logging
import play.api.http.Status.*
import uk.gov.hmrc.http.{HttpException, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class HttpClientResponse @Inject() ()(implicit ec: ExecutionContext) extends Logging {

  private def logErrorResponses[T]: PartialFunction[Try[Either[UpstreamErrorResponse, T]], Either[UpstreamErrorResponse, T]] = {
    case Success(Left(r)) if r.statusCode == NOT_FOUND =>
      logger.warn(s"Response of ${r.statusCode} returned", r)
      Left[UpstreamErrorResponse, T](UpstreamErrorResponse(s"Transfer not found: ${r.message}", NOT_FOUND))
    case Success(Left(r))                              =>
      logger.warn(s"Response of ${r.statusCode} returned", r)
      Left[UpstreamErrorResponse, T](UpstreamErrorResponse(s"Error retrieving transfer: ${r.message}", r.statusCode))
    case Failure(ex: HttpException)                    =>
      logger.error(s"HttpException thrown: ${ex.message}", ex)
      Left(
        UpstreamErrorResponse(message = ex.message, statusCode = ex.responseCode, reportAs = BAD_GATEWAY)
      )
  }

  def read[T](
      response: Future[Either[UpstreamErrorResponse, T]]
    ): Future[Either[UpstreamErrorResponse, T]] =
    response andThen logErrorResponses[T]
}
