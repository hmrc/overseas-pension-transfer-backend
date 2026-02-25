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

package uk.gov.hmrc.overseaspensiontransferbackend.models

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Format, JsPath, Reads, Writes}
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication.PsaId._
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication.{PsaId, PsaPspId, UserType}

case class QtDeclaration(
    submittedBy: UserType,
    submitterId: PsaPspId,
    psaId: Option[PsaId]
  )

object QtDeclaration {

  implicit val reads: Reads[QtDeclaration] = (
    (JsPath \ "submittedBy").read[UserType] and
      (JsPath \ "submitterId").read[PsaPspId] and
      (JsPath \ "psaId").readNullable[PsaId](downstreamReads)
  )(QtDeclaration.apply _)

  implicit val writes: Writes[QtDeclaration] = (
    (JsPath \ "submittedBy").write[UserType] and
      (JsPath \ "submitterId").write[PsaPspId] and
      (JsPath \ "psaId").writeNullable[PsaId](downstreamWrites)
  )(qtDec => (qtDec.submittedBy, qtDec.submitterId, qtDec.psaId))

  implicit val format: Format[QtDeclaration] = Format(reads, writes)
}
