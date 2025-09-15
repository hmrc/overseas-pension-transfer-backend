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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.SubmissionConnector
import uk.gov.hmrc.overseaspensiontransferbackend.models.{PstrNumber, QtStatus, SavedUserAnswers}
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission._
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream._
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.validators._

import java.time.LocalDate
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

  private val downstreamSuccess = DownstreamSubmittedSuccess(
    qtNumber         = QtNumber("QT123456"),
    processingDate   = now,
    formBundleNumber = "119000004320"
  )

  "submitAnswers" - {

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
      val downstreamErrors: List[DownstreamSubmittedError] = List(
        EtmpValidationSubmittedError(processingDate = "2025-07-01T09:30:00Z", code = "003", text = "Request could not be processed"),
        HipBadRequest(
          origin                                    = "HoD",
          code                                      = "400",
          message                                   = "Invalid JSON",
          logId                                     = Some("ABCDEF0123456789ABCDEF0123456789")
        ),
        HipOriginFailures(origin                    = "HIP", failures              = List(HipOriginFailures.Failure("Type", "Reason"))),
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
      val infra: List[DownstreamSubmittedError] = List(
        Unauthorized,
        Forbidden,
        NotFound,
        ServerSubmittedError$,
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
  "getAllSubmissions" - {
    "must map downstream overview rows into SubmissionGetAllItem and wrap in Right" in {
      val pstr = PstrNumber("24000001AA")

      val ds = DownstreamGetAllSuccess(
        DownstreamGetAllSuccess.Payload(
          qropsTransferOverview = List(
            DownstreamGetAllSuccess.OverviewItem(
              fbNumber                  = "123456000023",
              qtReference               = "QT564321",
              qtVersion                 = "001",
              qtStatus                  = "Compiled",
              qtDigitalStatus           = "Complied",
              nino                      = "AA000000A",
              firstName                 = "David",
              lastName                  = "Warne",
              qtDate                    = LocalDate.parse("2025-03-14"),
              qropsReference            = "QROPS654321",
              submissionCompilationDate = now
            ),
            DownstreamGetAllSuccess.OverviewItem(
              fbNumber                  = "123456000024",
              qtReference               = "QT564322",
              qtVersion                 = "003",
              qtStatus                  = "Submitted",
              qtDigitalStatus           = "Submitted",
              nino                      = "AA000001A",
              firstName                 = "Edith",
              lastName                  = "Ennis-Hill",
              qtDate                    = LocalDate.parse("2025-01-01"),
              qropsReference            = "QROPS654322",
              submissionCompilationDate = now
            )
          )
        )
      )

      when(mockConnector.getAllSubmissions(eqTo(pstr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(ds)))

      val result = service.getAllSubmissions(pstr).futureValue

      result match {
        case Right(SubmissionGetAllResponse(items)) =>
          items must have size 2

          val first = items.head
          first.transferReference mustBe None
          first.qtReference       mustBe Some(QtNumber("QT564321"))
          first.nino              mustBe Some("AA000000A")
          first.memberFirstName   mustBe Some("David")
          first.memberSurname     mustBe Some("Warne")
          first.submissionDate    mustBe Some(LocalDate.parse("2025-03-14"))
          first.qtStatus          mustBe Some(QtStatus("Compiled"))
          first.schemeId          mustBe Some(pstr)

          val second = items(1)
          second.qtReference     mustBe Some(QtNumber("QT564322"))
          second.memberFirstName mustBe Some("Edith")
          second.memberSurname   mustBe Some("Ennis-Hill")

        case other =>
          fail(s"Unexpected: $other")
      }
    }

    "must return Left(SubmissionGetAllError()) when connector returns an error" in {
      val pstr = PstrNumber("24000001AA")

      val dsError = mock[DownstreamGetAllError]
      when(mockConnector.getAllSubmissions(eqTo(pstr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(dsError)))

      val result = service.getAllSubmissions(pstr).futureValue
      result mustBe Left(SubmissionGetAllError())
    }

    "must handle empty downstream overview list by returning an empty submissions list" in {
      val pstr = PstrNumber("24000001AA")

      val ds = DownstreamGetAllSuccess(
        DownstreamGetAllSuccess.Payload(Nil)
      )

      when(mockConnector.getAllSubmissions(eqTo(pstr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(ds)))

      val result = service.getAllSubmissions(pstr).futureValue
      result mustBe Right(SubmissionGetAllResponse(Nil))
    }

  }

  "Dummy submitAnswers" - {
    "must always return QT123456 (temporary stub)" in {
      val dummy = new DummySubmissionServiceImpl()
      val out   = dummy.submitAnswers(normalisedSubmission).futureValue
      out mustBe Right(SubmissionResponse(QtNumber("QT123456")))
    }
  }
  "Dummy getAllSubmissions" - {
    "must return Right(SubmissionGetAllResponse(...)) for any PSTR" in {
      val dummy = new DummySubmissionServiceImpl()

      val pstr = "24000001AA"

      val result = dummy.getAllSubmissions(PstrNumber(pstr)).futureValue

      result mustBe Right(SubmissionGetAllResponse(Seq(SubmissionGetAllItem(None, None, None, None, None, None, None, None))))
    }
  }
}
