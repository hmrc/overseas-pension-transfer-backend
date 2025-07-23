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

package uk.gov.hmrc.overseaspensiontransferbackend.models.external

import uk.gov.hmrc.overseaspensiontransferbackend.models.{Enumerable, WithName}

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

sealed trait TaskField

object TaskField extends Enumerable.Implicits {

  case object MemberDetails             extends WithName("memberDetails") with TaskField
  case object QropsDetails              extends WithName("qropsDetails") with TaskField
  case object QropsSchemeManagerDetails extends WithName("qropsSchemeManagerDetails") with TaskField
  case object TransferDetails           extends WithName("transferDetails") with TaskField

  val values: Seq[TaskField] = Seq(
    MemberDetails,
    QropsDetails,
    QropsSchemeManagerDetails,
    TransferDetails
  )

  implicit val enumerable: Enumerable[TaskField] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
