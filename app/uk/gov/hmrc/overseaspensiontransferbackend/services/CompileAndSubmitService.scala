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

package uk.gov.hmrc.overseaspensiontransferbackend.services

import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.CompileAndSubmitConnector
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO.{fromUserAnswers, toUserAnswers}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait CompileAndSubmitService {
  def getAnswers(id: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswersDTO]]
  def upsertAnswers(answers: UserAnswersDTO)(implicit hc: HeaderCarrier): Future[Option[UserAnswersDTO]]
}

@Singleton
class CompileAndSubmitServiceImpl @Inject() (
    connector: CompileAndSubmitConnector
  )(implicit ec: ExecutionContext
  ) extends CompileAndSubmitService
    with Logging {
  // We should consider a typed error ADT or a sealed trait (like ServiceError) and handle errors in the controller for a consistent user-facing response.

  override def getAnswers(id: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswersDTO]] = {
    connector.getAnswers(id).map {
      case Right(userAnswers) => Some(fromUserAnswers(userAnswers))
      case Left(error)        =>
        if (error.statusCode == 404) None
        else throw error
    }
  }

  override def upsertAnswers(answersDTO: UserAnswersDTO)(implicit hc: HeaderCarrier): Future[Option[UserAnswersDTO]] = {
    connector
      // TODO: it seems likely we'll need another DTO and service layer here rather than sending UA to the API
      .upsertAnswers(toUserAnswers(answersDTO))
      .map {
        case Right(userAnswers) => Some(fromUserAnswers(userAnswers))
        case Left(error)        =>
          if (error.statusCode == 404) None
          else throw error
      }
  }
}
