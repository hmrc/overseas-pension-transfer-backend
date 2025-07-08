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

import org.mongodb.scala.SingleObservableFuture
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import play.api.libs.json.Format
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models.SavedUserAnswers
import uk.gov.hmrc.play.http.logging.Mdc

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

  private def byId(id: String): Bson = Filters.equal("_id", id)

  private def byReferenceId(referenceId: String): Bson = Filters.equal("referenceId", referenceId)

  def keepAlive(referenceId: String): Future[Boolean] = Mdc.preservingMdc {
    collection
      .updateOne(
        filter = byReferenceId(referenceId),
        update = Updates.set("lastUpdated", Instant.now(clock))
      )
      .toFuture()
      .map(_ => true)
  }

  def get(referenceId: String): Future[Option[SavedUserAnswers]] = Mdc.preservingMdc {
    keepAlive(referenceId).flatMap {
      _ =>
        collection
          .find(byReferenceId(referenceId))
          .headOption()
    }
  }

  def set(answers: SavedUserAnswers): Future[Boolean] = Mdc.preservingMdc {
    val updatedAnswers = answers copy (lastUpdated = Instant.now(clock))
    collection
      .updateOne(
        filter      = byReferenceId(updatedAnswers.referenceId),
        update      = Filters.equal("savedUserAnswers", updatedAnswers),
        options     = UpdateOptions().upsert(true)
      )
      .toFuture
      .map(_.wasAcknowledged())
  }

  def clear(referenceId: String): Future[Boolean] = Mdc.preservingMdc {
    collection
      .deleteOne(byReferenceId(referenceId))
      .toFuture()
      .map(_ => true)
  }
}
