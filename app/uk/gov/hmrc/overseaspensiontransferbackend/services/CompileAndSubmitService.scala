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
import uk.gov.hmrc.overseaspensiontransferbackend.models.UserAnswers

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait CompileAndSubmitService {
  def getAnswers(id: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]]
  def upsertAnswers(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers]
}

@Singleton
class CompileAndSubmitServiceImpl @Inject() (
                                              connector: CompileAndSubmitConnector
                                            )(implicit ec: ExecutionContext)
  extends CompileAndSubmitService
    with Logging {

  override def getAnswers(id: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] = {
    connector.getAnswers(id).map {
      case Right(userAnswers) => Some(userAnswers)
      case Left(error) =>
        if (error.statusCode == 404) None
        else throw error
    }.recover {
      case e: Exception =>
        logger.warn(s"Failed to get answers for ID '$id': ${e.getMessage}", e)
          None
    }
  }

  override def upsertAnswers(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[UserAnswers] = {
    connector
      .upsertAnswers(answers)
      .recover {
        case e: Exception =>
          logger.error(s"Failed to upsert answers for ID '${answers.id}': ${e.getMessage}", e)
          throw e
      }
  }
}