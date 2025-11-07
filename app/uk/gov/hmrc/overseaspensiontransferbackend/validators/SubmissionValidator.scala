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

package uk.gov.hmrc.overseaspensiontransferbackend.validators

import com.google.inject.ImplementedBy
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.overseaspensiontransferbackend.models._
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication._
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{NormalisedSubmission, QtNumber}

import javax.inject.{Inject, Singleton}

sealed trait ValidationResponse

final case class Submission(
    reportDetails: ReportDetails,
    transferringMember: Option[TransferringMember],
    aboutReceivingQROPS: Option[AboutReceivingQROPS],
    transferDetails: Option[TransferDetails],
    qtDeclaration: QtDeclaration,
    psaDeclaration: Option[Declaration],
    pspDeclaration: Option[Declaration]
  ) extends ValidationResponse

object Submission {

  def apply(answers: SavedUserAnswers, normalised: NormalisedSubmission): Submission = {
    val reportDetails = {
      val maybeQtReference = normalised.referenceId match {
        case QtNumber(value) => Some(value)
        case _               => None
      }
      ReportDetails(
        answers.pstr.value,
        Submitted,
        maybeQtReference,
        None
      )
    }

    val qtDeclaration =
      QtDeclaration(
        normalised.userId.userType,
        normalised.userId,
        normalised.maybeAssociatedPsaId
      )

    val (psaDeclaration, pspDeclaration): (Option[Declaration], Option[Declaration]) = {
      val buildDeclaration: Option[Declaration] = answers.data.submitToHMRC match {
        case Some(true) => Some(Declaration(declaration1 = true, declaration2 = true))
        case None       => None
      }

      normalised.userId.userType match {
        case Psa => (buildDeclaration, None)
        case Psp => (None, buildDeclaration)
      }
    }

    Submission(
      reportDetails,
      answers.data.transferringMember,
      answers.data.aboutReceivingQROPS,
      answers.data.transferDetails,
      qtDeclaration,
      psaDeclaration,
      pspDeclaration
    )
  }
  implicit val writes: Writes[Submission]                                            = Json.writes
}
final case class ValidationError(message: String) extends ValidationResponse

@ImplementedBy(classOf[SubmissionValidatorImpl])
trait SubmissionValidator {
  def validate(prepared: Submission): Either[ValidationError, Submission]
}

@Singleton
class SubmissionValidatorImpl @Inject() extends SubmissionValidator {

  override def validate(prepared: Submission): Either[ValidationError, Submission] =
    (prepared.qtDeclaration.submittedBy, prepared.psaDeclaration, prepared.pspDeclaration) match {
      case (Psa, Some(Declaration(true, true)), None) => Right(prepared)
      case (Psa, None, _)                             => Left(ValidationError("PsaDeclaration missing from submission"))
      case (Psp, None, Some(Declaration(true, true))) =>
        prepared.qtDeclaration.psaId match {
          case Some(_) => Right(prepared)
          case None    => Left(ValidationError("Authorising PsaId is missing from Submission"))
        }
      case (Psp, _, None)                             => Left(ValidationError("PspDeclaration missing from submission"))
      case _                                          => Left(ValidationError("Submission payload is invalid"))
    }
}
