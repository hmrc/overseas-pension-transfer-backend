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
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{AllTransfersItem, TransferId}

import java.time.Instant

final case class SeedAmendInProgress(
    pstr: String,
    transferReference: TransferId,
    lastUpdated: Instant,
    nino: Option[String]      = None,
    firstName: Option[String] = None,
    lastName: Option[String]  = None
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
      transferId      = transferReference,
      qtVersion       = None,
      nino            = nino,
      memberFirstName = firstName,
      memberSurname   = lastName,
      submissionDate  = None,
      lastUpdated     = Some(lastUpdated),
      qtStatus        = Some(AmendInProgress),
      pstrNumber      = Some(PstrNumber(pstr)),
      qtDate          = None
    )

  def toSavedUserAnswers: SavedUserAnswers =
    SavedUserAnswers(
      transferId  = transferReference,
      pstr        = PstrNumber(pstr),
      data        = toAnswersData,
      lastUpdated = lastUpdated
    )
}

object SeedAmendInProgress {
  implicit val format: OFormat[SeedAmendInProgress] = Json.format[SeedAmendInProgress]

  def toItems(seeds: Seq[SeedAmendInProgress]): Seq[AllTransfersItem] =
    seeds.map(_.toItem)
}
