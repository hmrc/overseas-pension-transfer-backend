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

import uk.gov.hmrc.overseaspensiontransferbackend.models.SavedUserAnswers
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO.toSavedUserAnswers
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

//I am making this return an option for ease of use, but we should consider making this return either SavedUserAnswers or an Error

trait SaveForLaterService {
  def getAnswers(id: String)(implicit hc: HeaderCarrier): Future[Option[SavedUserAnswers]]
  def saveAnswers(answers: UserAnswersDTO)(implicit hc: HeaderCarrier): Future[Option[SavedUserAnswers]]
}

@Singleton
class SaveForLaterServiceImpl @Inject() (
    repository: SaveForLaterRepository
  )(implicit ec: ExecutionContext
  ) extends SaveForLaterService
    with Logging {

  override def getAnswers(id: String)(implicit hc: HeaderCarrier): Future[Option[SavedUserAnswers]] = {
    repository.get(id)
  }

  override def saveAnswers(answersDTO: UserAnswersDTO)(implicit hc: HeaderCarrier): Future[Option[SavedUserAnswers]] = {
    val savedUserAnswers = toSavedUserAnswers(answersDTO)
    repository
      .set(savedUserAnswers)
      .map {
        case true => Some(savedUserAnswers)
        case _    => None
      }
  }
}
