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

package uk.gov.hmrc.overseaspensiontransferbackend.models.internal

import uk.gov.hmrc.overseaspensiontransferbackend.models.{Enumerable, WithName}

sealed trait AnswersDataField

object AnswersDataField extends Enumerable.Implicits {

  case object TransferringMember   extends WithName("transferringMember") with AnswersDataField
  case object AboutReceivingQROPS  extends WithName("aboutReceivingQROPS") with AnswersDataField
  case object SchemeManagerDetails extends WithName("schemeManagerDetails") with AnswersDataField
  case object TransferDetails      extends WithName("transferDetails") with AnswersDataField

  val values: Seq[AnswersDataField] = Seq(
    TransferringMember,
    AboutReceivingQROPS,
    SchemeManagerDetails,
    TransferDetails
  )

  implicit val enumerable: Enumerable[AnswersDataField] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
