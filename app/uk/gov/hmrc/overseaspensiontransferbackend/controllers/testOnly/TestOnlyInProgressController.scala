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

package uk.gov.hmrc.overseaspensiontransferbackend.controllers.testOnly

import play.api.libs.json.{JsError, JsValue}
import play.api.mvc._
import uk.gov.hmrc.overseaspensiontransferbackend.models.PstrNumber
import uk.gov.hmrc.overseaspensiontransferbackend.models.testOnly.SeedInProgress
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.utils.testOnly.NameGenerator
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.{LocalDate, ZoneOffset}
import java.util.concurrent.ThreadLocalRandom
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TestOnlyInProgressController @Inject() (
    cc: ControllerComponents,
    saveForLaterRepo: SaveForLaterRepository
  )(implicit ec: ExecutionContext
  ) extends BackendController(cc) {

  def seed: Action[JsValue] = Action.async(parse.json) { implicit req =>
    req.body.validate[SeedInProgress].fold(
      e => Future.successful(BadRequest(JsError.toJson(e))),
      s => saveForLaterRepo.set(s.toSavedUserAnswers).map(_ => Created)
    )
  }

  def bulk: Action[JsValue] = Action.async(parse.json) { implicit req =>
    req.body.validate[Seq[SeedInProgress]].fold(
      e => Future.successful(BadRequest(JsError.toJson(e))),
      seeds => Future.sequence(seeds.map(s => saveForLaterRepo.set(s.toSavedUserAnswers))).map(_ => Created)
    )
  }

  def generate(pstr: String, n: Int): Action[AnyContent] = Action.async {
    val today       = LocalDate.now(ZoneOffset.UTC)
    val maxDaysBack = 30
    val rng         = ThreadLocalRandom.current()
    val seeds       = (1 to n).map { i =>
      val (first, last) = NameGenerator.nameFor(pstr, i)
      val daysBack      = rng.nextLong(0L, maxDaysBack + 1L)
      val lastUpdated   = today.minusDays(daysBack)
      SeedInProgress(
        pstr              = pstr,
        transferReference = s"T-$i",
        lastUpdated       = lastUpdated,
        nino              = Some(f"AA0000$i%02dA"),
        firstName         = Some(first),
        lastName          = Some(last)
      )
    }
    Future.sequence(seeds.map(s => saveForLaterRepo.set(s.toSavedUserAnswers))).map(_ => Created)
  }

  def clear(pstr: String): Action[AnyContent] = Action.async {
    saveForLaterRepo.testOnlyDeleteByPstr(PstrNumber(pstr)).map(_ => NoContent)
  }
}
