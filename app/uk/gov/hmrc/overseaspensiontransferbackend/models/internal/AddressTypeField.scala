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

sealed trait AddressTypeField

object AddressTypeField extends Enumerable.Implicits {

  case object PrincipalResAddDetails  extends WithName("principalResAddDetails") with AddressTypeField
  case object LastPrincipalAddDetails extends WithName("lastPrincipalAddDetails") with AddressTypeField
  case object PropertyAddress         extends WithName("propertyAddress") with AddressTypeField
  case object ReceivingQropsAddress   extends WithName("receivingQropsAddress") with AddressTypeField
  case object SchemeManagerAddress    extends WithName("schemeManagerAddress") with AddressTypeField

  val values: Seq[AddressTypeField] = Seq(
    PrincipalResAddDetails,
    LastPrincipalAddDetails,
    PropertyAddress,
    ReceivingQropsAddress,
    SchemeManagerAddress
  )

  implicit val enumerable: Enumerable[AddressTypeField] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
