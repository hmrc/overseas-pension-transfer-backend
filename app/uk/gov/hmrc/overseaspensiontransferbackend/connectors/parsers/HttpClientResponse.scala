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

import com.google.inject.Inject
import play.api.Logging
import play.api.http.Status.*
import uk.gov.hmrc.http.{HttpException, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class HttpClientResponse @Inject() ()(implicit ec: ExecutionContext) extends Logging {

  private val logErrorResponses: PartialFunction[Try[Either[UpstreamErrorResponse, HttpResponse]], Unit] = {
    case Success(Left(r))           => logger.error(s"Response of ${r.statusCode} returned", r)
    case Failure(ex: HttpException) => logger.error(s"HttpException thrown: ${ex.message}", ex)
  }

  private val recoverHttpException: PartialFunction[Throwable, Either[UpstreamErrorResponse, HttpResponse]] = {
    case exception: HttpException =>
      Left(
        UpstreamErrorResponse(message = exception.message, statusCode = exception.responseCode, reportAs = BAD_GATEWAY)
      )
  }

  def read(
      response: Future[Either[UpstreamErrorResponse, HttpResponse]]
    ): Future[Either[UpstreamErrorResponse, HttpResponse]] =
    response andThen logErrorResponses recover recoverHttpException
}
