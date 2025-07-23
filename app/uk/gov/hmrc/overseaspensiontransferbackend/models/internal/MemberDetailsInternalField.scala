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

sealed trait MemberDetailsInternalField

object MemberDetailsInternalField extends Enumerable.Implicits {

  case object ForeName               extends WithName("foreName") with MemberDetailsInternalField
  case object LastName               extends WithName("lastName") with MemberDetailsInternalField
  case object DateOfBirth            extends WithName("dateOfBirth") with MemberDetailsInternalField
  case object Nino                   extends WithName("nino") with MemberDetailsInternalField
  case object MemberNoNino           extends WithName("memberNoNino") with MemberDetailsInternalField
  case object PrincipalResAddDetails extends WithName("principalResAddDetails") with MemberDetailsInternalField
  case object MemberResidencyDetails extends WithName("memberResidencyDetails") with MemberDetailsInternalField

  val values: Seq[MemberDetailsInternalField] = Seq(
    ForeName,
    LastName,
    DateOfBirth,
    Nino,
    MemberNoNino,
    PrincipalResAddDetails,
    MemberResidencyDetails
  )

  implicit val enumerable: Enumerable[MemberDetailsInternalField] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
