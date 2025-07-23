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

sealed trait MemberDetailsExternalField

object MemberDetailsExternalField extends Enumerable.Implicits {

  case object Name                     extends WithName("name") with MemberDetailsExternalField
  case object Nino                     extends WithName("nino") with MemberDetailsExternalField
  case object ReasonNoNINO             extends WithName("reasonNoNINO") with MemberDetailsExternalField
  case object DateOfBirth              extends WithName("dateOfBirth") with MemberDetailsExternalField
  case object PrincipalResAddDetails   extends WithName("principalResAddDetails") with MemberDetailsExternalField
  case object MemUkResident            extends WithName("memUkResident") with MemberDetailsExternalField
  case object MemEverUkResident        extends WithName("memEverUkResident") with MemberDetailsExternalField
  case object LastPrincipalAddDetails  extends WithName("lastPrincipalAddDetails") with MemberDetailsExternalField
  case object DateMemberLeftUkExternal extends WithName("dateMemberLeftUk") with MemberDetailsExternalField

  val values: Seq[MemberDetailsExternalField] = Seq(
    Name,
    Nino,
    ReasonNoNINO,
    DateOfBirth,
    PrincipalResAddDetails,
    MemUkResident,
    MemEverUkResident,
    LastPrincipalAddDetails,
    DateMemberLeftUkExternal
  )

  implicit val enumerable: Enumerable[MemberDetailsExternalField] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
