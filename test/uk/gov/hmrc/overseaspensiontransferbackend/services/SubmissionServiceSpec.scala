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

package uk.gov.hmrc.overseaspensiontransferbackend.services

import org.scalatest.freespec.AnyFreeSpec
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.SubmissionConnector
import uk.gov.hmrc.overseaspensiontransferbackend.models.SavedUserAnswers
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission._
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream._
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.validators._

import scala.concurrent.Future

class SubmissionServiceSpec extends AnyFreeSpec with SpecBase {

  private val mockRepo      = mock[SaveForLaterRepository]
  private val mockValidator = mock[SubmissionValidator]
  private val mockConnector = mock[SubmissionConnector]
  private val service       = new SubmissionServiceImpl(mockRepo, mockValidator, mockConnector)

  private val normalisedSubmission = NormalisedSubmission(
    referenceId = testId,
    submitter   = Submitter.PsaSubmitter(PsaId("A1234567")),
    psaId       = PsaId("A1234567"),
    lastUpdated = now
  )

  private val saved: SavedUserAnswers = simpleSavedUserAnswers

  private val downstreamSuccess = DownstreamSuccess(
    qtNumber         = QtNumber("QT123456"),
    processingDate   = now,
    formBundleNumber = "119000004320"
  )

  "SubmissionServiceImpl" - {

    "must return Right(SubmissionResponse) on happy path" in {
      when(mockRepo.get(eqTo(testId))).thenReturn(Future.successful(Some(saved)))
      when(mockValidator.validate(eqTo(saved))).thenReturn(Right(ValidatedSubmission(saved)))
      when(mockConnector.submit(eqTo(ValidatedSubmission(saved)))(any))
        .thenReturn(Future.successful(Right(downstreamSuccess)))

      val result = service.submitAnswers(normalisedSubmission).futureValue
      result mustBe Right(SubmissionResponse(QtNumber("QT123456")))
    }

    "must return Left(SubmissionTransformationError) when no prepared submission found" in {
      when(mockRepo.get(eqTo(testId))).thenReturn(Future.successful(None))

      val result = service.submitAnswers(normalisedSubmission).futureValue
      result match {
        case Left(SubmissionTransformationError(msg)) =>
          msg must include(testId)
        case other                                    => fail(s"Unexpected: $other")
      }
    }

    "must return Left(SubmissionTransformationError) when validator fails" in {
      when(mockRepo.get(eqTo(testId))).thenReturn(Future.successful(Some(saved)))
      when(mockValidator.validate(eqTo(saved))).thenReturn(Left(ValidationError("boom")))

      val result = service.submitAnswers(normalisedSubmission).futureValue
      result mustBe Left(SubmissionTransformationError("boom"))
    }

    "must map validation-type downstream errors to SubmissionTransformationError" in {
      val downstreamErrors: List[DownstreamError] = List(
        EtmpValidationError(processingDate = "2025-07-01T09:30:00Z", code = "003", text    = "Request could not be processed"),
        HipBadRequest(origin               = "HoD", code                  = "400", message = "Invalid JSON", logId = Some("ABCDEF0123456789ABCDEF0123456789")),
        HipOriginFailures(origin           = "HIP", failures              = List(HipOriginFailures.Failure("Type", "Reason"))),
        UnsupportedMedia
      )

      when(mockRepo.get(eqTo(testId))).thenReturn(Future.successful(Some(saved)))
      when(mockValidator.validate(eqTo(saved))).thenReturn(Right(ValidatedSubmission(saved)))

      downstreamErrors.foreach { ue =>
        when(mockConnector.submit(eqTo(ValidatedSubmission(saved)))(any))
          .thenReturn(Future.successful(Left(ue)))

        val result = service.submitAnswers(normalisedSubmission).futureValue
        result match {
          case Left(SubmissionTransformationError(msg)) =>
            msg must include("Submission failed validation")
          case other                                    =>
            fail(s"Expected SubmissionTransformationError for $ue, got $other")
        }
      }
    }

    "must map infrastructural downstream errors to SubmissionFailed" in {
      val infra: List[DownstreamError] = List(
        Unauthorized,
        Forbidden,
        NotFound,
        ServerError,
        ServiceUnavailable,
        Unexpected(777, "<body>")
      )

      when(mockRepo.get(eqTo(testId))).thenReturn(Future.successful(Some(saved)))
      when(mockValidator.validate(eqTo(saved))).thenReturn(Right(ValidatedSubmission(saved)))

      infra.foreach { ue =>
        when(mockConnector.submit(eqTo(ValidatedSubmission(saved)))(any))
          .thenReturn(Future.successful(Left(ue)))

        val result = service.submitAnswers(normalisedSubmission).futureValue
        result mustBe Left(SubmissionFailed)
      }
    }
  }

  "DummySubmissionServiceImpl" - {
    "must always return QT123456 (temporary stub)" in {
      val dummy = new DummySubmissionServiceImpl()
      val out   = dummy.submitAnswers(normalisedSubmission).futureValue
      out mustBe Right(SubmissionResponse(QtNumber("QT123456")))
    }
  }
}
