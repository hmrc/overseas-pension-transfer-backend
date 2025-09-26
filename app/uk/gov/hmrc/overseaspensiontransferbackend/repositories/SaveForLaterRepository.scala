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
import org.mongodb.scala.bson._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model._
import play.api.Logging
import play.api.libs.json.Format
import uk.gov.hmrc.mdc.Mdc
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models._
import uk.gov.hmrc.overseaspensiontransferbackend.services.EncryptionService

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SaveForLaterRepository @Inject() (
    mongoComponent: MongoComponent,
    encryptionService: EncryptionService,
    appConfig: AppConfig,
    clock: Clock
  )(implicit ec: ExecutionContext
  ) extends Logging {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private val collection =
    mongoComponent.database.getCollection[Document]("saved-user-answers")

  initIndexes()

  private def initIndexes(): Unit =
    collection.createIndex(
      Indexes.ascending("lastUpdated"),
      IndexOptions().name("lastUpdatedIdx").expireAfter(appConfig.cacheTtl, TimeUnit.DAYS)
    ).toFuture()
      .recover { case ex => logger.warn("Failed to create TTL index on saved-user-answers", ex) }
      .foreach(_ => logger.info("TTL index ensured on saved-user-answers"))

  private def byReferenceId(referenceId: String) = Filters.equal("referenceId", referenceId)

  private def toDocument(answers: SavedUserAnswers): Document = {
    val encrypted: EncryptedAnswersData =
      DecryptedAnswersData(answers.data).encrypt(appConfig, encryptionService)

    Document(
      "referenceId" -> answers.referenceId,
      "data"        -> encrypted.encryptedString,
      "lastUpdated" -> BsonDateTime(answers.lastUpdated.toEpochMilli)
    )
  }

  private def fromDocument(doc: Document): Option[SavedUserAnswers] =
    for {
      referenceId <- doc.get("referenceId").collect { case bs: BsonString => bs.getValue }
      dataEnc     <- doc.get("data").collect { case bs: BsonString => bs.getValue }
      lastUpdated <- doc.get("lastUpdated").collect { case d: BsonDateTime => Instant.ofEpochMilli(d.getValue) }
      decrypted   <- EncryptedAnswersData(dataEnc).decrypt(appConfig, encryptionService).toOption
    } yield {
      SavedUserAnswers(referenceId, decrypted.data, lastUpdated)
    }

  // === Public API ===

  def get(referenceId: String): Future[Option[SavedUserAnswers]] = Mdc.preservingMdc {
    collection.find(byReferenceId(referenceId))
      .headOption()
      .map(_.flatMap(fromDocument))
  }

  def set(answers: SavedUserAnswers): Future[Boolean] = Mdc.preservingMdc {
    collection.replaceOne(
      filter      = byReferenceId(answers.referenceId),
      replacement = toDocument(answers),
      options     = ReplaceOptions().upsert(true)
    ).toFuture().map(_.wasAcknowledged())
  }

  def clear(referenceId: String): Future[Boolean] = Mdc.preservingMdc {
    collection.deleteOne(byReferenceId(referenceId)).toFuture().map(_.wasAcknowledged())
  }

  /** Safely delete all documents, keeping the collection and indexes */
  def clearAll(): Future[Done] = Mdc.preservingMdc {
    collection.deleteMany(Document()).toFuture().map(_ => Done)
  }
}
