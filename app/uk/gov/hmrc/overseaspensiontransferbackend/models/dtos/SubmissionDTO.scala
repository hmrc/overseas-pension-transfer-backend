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

package uk.gov.hmrc.overseaspensiontransferbackend.models.dtos

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication._
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer._

import java.time.Instant

sealed trait SubmissionDTO {
  def referenceId: TransferId
  def userType: UserType
  def lastUpdated: Instant

  def normalise(withReferenceId: TransferId, authenticatedUser: AuthenticatedUser): NormalisedSubmission
}

final case class PsaSubmissionDTO(
    referenceId: TransferId,
    userType: UserType = Psa,
    userId: PsaId,
    lastUpdated: Instant
  ) extends SubmissionDTO {

  override def normalise(withReferenceId: TransferId, authenticatedUser: AuthenticatedUser): NormalisedSubmission = {
    NormalisedSubmission(
      referenceId          = withReferenceId,
      userId               = userId,
      maybeAssociatedPsaId = None,
      lastUpdated          = lastUpdated,
      authenticatedUser    = authenticatedUser
    )
  }
}
object PsaSubmissionDTO { implicit val format: OFormat[PsaSubmissionDTO] = Json.format }

final case class PspSubmissionDTO(
    referenceId: TransferId,
    userType: UserType = Psp,
    userId: PspId,
    psaId: PsaId,
    lastUpdated: Instant
  ) extends SubmissionDTO {

  override def normalise(withReferenceId: TransferId, authenticatedUser: AuthenticatedUser): NormalisedSubmission =
    NormalisedSubmission(
      referenceId          = withReferenceId,
      userId               = userId,
      maybeAssociatedPsaId = Some(psaId),
      lastUpdated          = lastUpdated,
      authenticatedUser    = authenticatedUser
    )
}
object PspSubmissionDTO { implicit val format: OFormat[PspSubmissionDTO] = Json.format }

object SubmissionDTO {

  implicit val format: OFormat[SubmissionDTO] = {
    val psaR: Reads[SubmissionDTO] = Json.reads[PsaSubmissionDTO].map(identity[SubmissionDTO])
    val pspR: Reads[SubmissionDTO] = Json.reads[PspSubmissionDTO].map(identity[SubmissionDTO])

    val reads: Reads[SubmissionDTO] =
      (__ \ "userType").read[UserType].flatMap {
        case Psa => psaR
        case Psp => pspR
      }

    val writes: OWrites[SubmissionDTO] = {
      case x: PsaSubmissionDTO => Json.writes[PsaSubmissionDTO].writes(x)
      case x: PspSubmissionDTO => Json.writes[PspSubmissionDTO].writes(x)
    }

    OFormat(reads, writes)
  }
}
