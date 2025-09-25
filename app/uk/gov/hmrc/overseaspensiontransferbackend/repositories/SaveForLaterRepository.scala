/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.overseaspensiontransferbackend.repositories

import org.apache.pekko.Done
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import play.api.libs.json.Format
import uk.gov.hmrc.mdc.Mdc
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.AllTransfersItem
import uk.gov.hmrc.overseaspensiontransferbackend.models.{PstrNumber, SavedUserAnswers}

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SaveForLaterRepository @Inject() (
    mongoComponent: MongoComponent,
    appConfig: AppConfig,
    clock: Clock
  )(implicit ec: ExecutionContext
  ) extends PlayMongoRepository[SavedUserAnswers](
      collectionName = "saved-user-answers",
      mongoComponent = mongoComponent,
      domainFormat   = SavedUserAnswers.format,
      indexes        = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.cacheTtl, TimeUnit.DAYS)
        )
      )
    ) {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byId(id: String): Bson   = Filters.equal("_id", id)
  private def byPstr(pstr: PstrNumber): Bson = Filters.equal("pstr", pstr.value)

  def get(referenceId: String): Future[Option[SavedUserAnswers]] = Mdc.preservingMdc {
    collection
      .find(byId(referenceId))
      .headOption()
  }

  def getRecords(pstr: PstrNumber): Future[Seq[AllTransfersItem]] = Mdc.preservingMdc {
    collection
      .find(byPstr(pstr))
      .toFuture()
      .map(
        _.map(_.toAllTransfersItem)
      )
  }

  def set(answers: SavedUserAnswers): Future[Boolean] = Mdc.preservingMdc {
    collection
      .replaceOne(
        filter      = byId(answers.referenceId),
        replacement = answers,
        options     = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_.wasAcknowledged())
  }

  def clear(referenceId: String): Future[Boolean] = Mdc.preservingMdc {
    collection
      .deleteOne(byId(referenceId))
      .toFuture()
      .map(_ => true)
  }

  def clear: Future[Done] = Mdc.preservingMdc {
    collection.drop().toFuture().map(_ => Done)
  }
}
