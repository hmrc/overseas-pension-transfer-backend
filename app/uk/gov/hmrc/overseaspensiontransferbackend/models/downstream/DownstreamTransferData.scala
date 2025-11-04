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

package uk.gov.hmrc.overseaspensiontransferbackend.models.downstream

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{__, Reads}
import uk.gov.hmrc.overseaspensiontransferbackend.models._

case class DownstreamTransferData(
    pstr: PstrNumber,
    qtDetails: QtDetails,
    transferringMember: Option[TransferringMember],
    aboutReceivingQROPS: Option[AboutReceivingQROPS],
    transferDetails: Option[TransferDetails]
  ) {

  def toSavedUserAnswers: SavedUserAnswers =
    SavedUserAnswers(
      qtDetails.qtReference,
      pstr,
      AnswersData(
        transferringMember,
        aboutReceivingQROPS,
        transferDetails,
        None
      ),
      qtDetails.receiptDate
    )
}

object DownstreamTransferData {

  implicit val reads: Reads[DownstreamTransferData] = (
    (__ \ "success" \ "pstr").read[String].map(PstrNumber.apply) and
      (__ \ "success" \ "qtDetails").read[QtDetails] and
      (__ \ "success" \ "transferringMember").readNullable[TransferringMember] and
      (__ \ "success" \ "aboutReceivingQROPS").readNullable[AboutReceivingQROPS] and
      (__ \ "success" \ "transferDetails").readNullable[TransferDetails]
  )(DownstreamTransferData.apply _)
}
