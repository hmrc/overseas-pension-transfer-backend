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

package uk.gov.hmrc.overseaspensiontransferbackend.models.testOnly

import play.api.libs.json.{JsObject, Json, OFormat}
import uk.gov.hmrc.overseaspensiontransferbackend.models._
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{AllTransfersItem, QtNumber}

import java.time.{LocalDate, ZoneOffset}

final case class SeedInProgress(
    pstr: String,
    transferReference: String,
    lastUpdated: LocalDate,
    nino: Option[String]        = None,
    firstName: Option[String]   = None,
    lastName: Option[String]    = None,
    qtReference: Option[String] = None,
    qtVersion: Option[String]   = None
  ) {

  private def minimalAnswersDataJson: JsObject =
    Json.obj(
      "transferringMember" -> Json.obj(
        "memberDetails" -> Json.obj(
          "foreName" -> firstName,
          "lastName" -> lastName,
          "nino"     -> nino
        )
      )
    )

  private def toAnswersData: AnswersData =
    minimalAnswersDataJson.as[AnswersData]

  private def toItem: AllTransfersItem =
    AllTransfersItem(
      transferReference = Some(transferReference),
      qtReference       = qtReference.map(QtNumber.apply),
      qtVersion         = qtVersion,
      nino              = nino,
      memberFirstName   = firstName,
      memberSurname     = lastName,
      submissionDate    = None,
      lastUpdated       = Some(lastUpdated),
      qtStatus          = Some(InProgress),
      pstrNumber        = Some(PstrNumber(pstr))
    )

  def toSavedUserAnswers: SavedUserAnswers =
    SavedUserAnswers(
      referenceId = transferReference,
      pstr        = PstrNumber(pstr),
      data        = toAnswersData,
      lastUpdated = lastUpdated.atStartOfDay().toInstant(ZoneOffset.UTC)
    )
}

object SeedInProgress {
  implicit val format: OFormat[SeedInProgress] = Json.format[SeedInProgress]

  def toItems(seeds: Seq[SeedInProgress]): Seq[AllTransfersItem] =
    seeds.map(_.toItem)
}
