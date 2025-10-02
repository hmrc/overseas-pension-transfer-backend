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
import play.api.libs.json.{JsError, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.TransferConnector
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream._
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.{GetEtmpRecord, GetSaveForLaterRecord, UserAnswersDTO}
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer._
import uk.gov.hmrc.overseaspensiontransferbackend.models._
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.UserAnswersTransformer
import uk.gov.hmrc.overseaspensiontransferbackend.validators._

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class TransferServiceSpec extends AnyFreeSpec with SpecBase {

  private val mockRepo        = mock[SaveForLaterRepository]
  private val mockValidator   = mock[SubmissionValidator]
  private val mockConnector   = mock[TransferConnector]
  private val mockTransformer = mock[UserAnswersTransformer]
  private val service         = new TransferServiceImpl(mockRepo, mockValidator, mockTransformer, mockConnector)

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

  "TransferServiceImpl" - {
    "submitTransfer" - {
      "must return Right(SubmissionResponse) on happy path" in {
        when(mockRepo.get(eqTo(testId))).thenReturn(Future.successful(Some(saved)))
        when(mockValidator.validate(eqTo(saved))).thenReturn(Right(ValidatedSubmission(saved)))
        when(mockConnector.submitTransfer(eqTo(ValidatedSubmission(saved)))(any))
          .thenReturn(Future.successful(Right(downstreamSuccess)))

        val result = service.submitTransfer(normalisedSubmission).futureValue
        result mustBe Right(SubmissionResponse(QtNumber("QT123456")))
      }

      "must return Left(SubmissionTransformationError) when no prepared submission found" in {
        when(mockRepo.get(eqTo(testId))).thenReturn(Future.successful(None))

        val result = service.submitTransfer(normalisedSubmission).futureValue
        result match {
          case Left(SubmissionTransformationError(msg)) =>
            msg must include(testId)
          case other                                    => fail(s"Unexpected: $other")
        }
      }

      "must return Left(SubmissionTransformationError) when validator fails" in {
        when(mockRepo.get(eqTo(testId))).thenReturn(Future.successful(Some(saved)))
        when(mockValidator.validate(eqTo(saved))).thenReturn(Left(ValidationError("boom")))

        val result = service.submitTransfer(normalisedSubmission).futureValue
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
          when(mockConnector.submitTransfer(eqTo(ValidatedSubmission(saved)))(any))
            .thenReturn(Future.successful(Left(ue)))

          val result = service.submitTransfer(normalisedSubmission).futureValue
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
          when(mockConnector.submitTransfer(eqTo(ValidatedSubmission(saved)))(any))
            .thenReturn(Future.successful(Left(ue)))

          val result = service.submitTransfer(normalisedSubmission).futureValue
          result mustBe Left(SubmissionFailed)
        }
      }
    }
    "getTransfer" - {
      "return Right UserAnswersDTO" - {
        "searching for GetSaveForLaterRecord" in {
          val userAnswers = UserAnswersDTO(testId, PstrNumber("12345678AB"), Json.obj(), now)

          when(mockRepo.get(eqTo(testId))).thenReturn(Future.successful(Some(saved)))
          when(mockTransformer.deconstruct(any)).thenReturn(Right(Json.obj()))

          val result = await(service.getTransfer(Right(GetSaveForLaterRecord(testId, PstrNumber("12345678AB"), InProgress))))

          result mustBe Right(userAnswers)
        }

        List(Submitted, Compiled) foreach { qtStatus =>
          s"GetEtmpRecord with qtStatus ${qtStatus.downstreamValue} and transformer deconstructs correctly" in {
            val saved       = DownstreamTransferData(
              PstrNumber("12345678AB"),
              QtDetails("001", Submitted, now, QtNumber("QT123456"), None, None),
              None,
              None,
              None
            )
            val userAnswers = UserAnswersDTO("QT123456", PstrNumber("12345678AB"), Json.obj(), now)

            when(mockConnector.getTransfer(any, any, any)(any)).thenReturn(Future.successful(Right(saved)))
            when(mockTransformer.deconstruct(any)).thenReturn(Right(Json.obj()))

            val result = await(service.getTransfer(Right(GetEtmpRecord(QtNumber("QT123456"), PstrNumber("12345678AB"), qtStatus, "001"))))

            result mustBe Right(userAnswers)
          }
        }
      }

      "return Left TransferNotFound" - {
        "qtStatus is InProgress and Repo returns None" in {
          when(mockRepo.get(eqTo(testId))).thenReturn(Future.successful(None))

          val result = await(service.getTransfer(Right(GetSaveForLaterRecord(testId, PstrNumber("12345678AB"), InProgress))))

          result mustBe Left(TransferNotFound(s"Unable to find transferId: $testId from save-for-later"))
        }

        "qtStatus is Submitted and Repo returns None" in {
          when(mockConnector.getTransfer(any, any, any)(any)).thenReturn(Future.successful(Left(HipOriginFailures("Failed", List()))))

          val result = await(service.getTransfer(Right(GetEtmpRecord(QtNumber("QT123456"), PstrNumber("12345678AB"), Submitted, "001"))))

          result mustBe Left(TransferNotFound(s"Unable to find transferId: QT123456 from HoD"))
        }
      }

      "return Left TransferDeoncstructionError" - {
        "qtStatus is InProgress and deconstruct returns an error" in {
          when(mockRepo.get(eqTo(testId))).thenReturn(Future.successful(Some(saved)))
          when(mockTransformer.deconstruct(any)).thenReturn(Left(JsError("Error")))

          val result = await(service.getTransfer(Right(GetSaveForLaterRecord(testId, PstrNumber("12345678AB"), InProgress))))

          result mustBe Left(
            TransferDeconstructionError("Unable to deconstruct json with error: JsError(List((,List(JsonValidationError(List(Error),List())))))")
          )
        }

        "qtStatus is Compiled and deconstruct returns an error" in {
          val saved = DownstreamTransferData(
            PstrNumber("12345678AB"),
            QtDetails("001", Submitted, now, QtNumber("QT123456"), None, None),
            None,
            None,
            None
          )

          when(mockConnector.getTransfer(any, any, any)(any)).thenReturn(Future.successful(Right(saved)))
          when(mockTransformer.deconstruct(any)).thenReturn(Left(JsError("Error")))

          val result = await(service.getTransfer(Right(GetEtmpRecord(QtNumber("QT123456"), PstrNumber("12345678AB"), Compiled, "001"))))

          result mustBe Left(
            TransferDeconstructionError("Unable to deconstruct json with error: JsError(List((,List(JsonValidationError(List(Error),List())))))")
          )
        }
      }
    }
    "getAllTransfers" - {

      "must map downstream overview rows into AllTransfersItem and wrap in Right" in {
        val pstr           = PstrNumber("24000001AA")
        val submissionDate = now

        val ds = DownstreamAllTransfersData(
          DownstreamAllTransfersData.Payload(
            qropsTransferOverview = List(
              DownstreamAllTransfersData.OverviewItem(
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
                submissionCompilationDate = submissionDate
              ),
              DownstreamAllTransfersData.OverviewItem(
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
                submissionCompilationDate = submissionDate
              )
            )
          )
        )

        val toDate: LocalDate   = LocalDate.now(ZoneOffset.UTC)
        val fromDate: LocalDate = toDate.minusYears(10)

        when(mockConnector.getAllTransfers(eqTo(pstr), eqTo(fromDate), eqTo(toDate), eqTo(None))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(ds)))

        // no in-progress
        when(mockRepo.getRecords(eqTo(pstr))).thenReturn(Future.successful(Nil))

        when(mockAppConfig.getAllTransfersYearsOffset).thenReturn(10)

        val result = service.getAllTransfers(pstr).futureValue

        result match {
          case Right(AllTransfersResponse(items)) =>
            items must have size 2

            val first = items.head
            first.transferReference mustBe None
            first.qtReference       mustBe Some(QtNumber("QT564321"))
            first.qtVersion         mustBe Some("001")
            first.qtStatus          mustBe Some(QtStatus("Compiled"))
            first.nino              mustBe Some("AA000000A")
            first.memberFirstName   mustBe Some("David")
            first.memberSurname     mustBe Some("Warne")
            first.qtDate            mustBe Some(LocalDate.parse("2025-03-14"))
            first.pstrNumber        mustBe Some(pstr)
            first.submissionDate    mustBe Some(submissionDate)

            val second = items(1)
            second.qtReference     mustBe Some(QtNumber("QT564322"))
            second.memberFirstName mustBe Some("Edith")
            second.memberSurname   mustBe Some("Ennis-Hill")

          case other =>
            fail(s"Unexpected: $other")
        }
      }

      "returns Left(NoTransfersFound) when connector returns NotFound and no in-progress exists" in {
        val pstr = PstrNumber("24000001AA")

        val toDate: LocalDate   = LocalDate.now(ZoneOffset.UTC)
        val fromDate: LocalDate = toDate.minusYears(10)

        when(mockConnector.getAllTransfers(eqTo(pstr), eqTo(fromDate), eqTo(toDate), eqTo(None))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(NoTransfersFound)))

        when(mockRepo.getRecords(eqTo(pstr))).thenReturn(Future.successful(Nil))

        when(mockAppConfig.getAllTransfersYearsOffset).thenReturn(10)

        val result = service.getAllTransfers(pstr).futureValue
        result mustBe Left(NoTransfersFoundResponse)
      }

      "returns Left(UnexpectedError) when connector returns any other error and no in-progress exists" in {
        val pstr = PstrNumber("24000001AA")

        val toDate: LocalDate   = LocalDate.now(ZoneOffset.UTC)
        val fromDate: LocalDate = toDate.minusYears(10)

        when(mockConnector.getAllTransfers(eqTo(pstr), eqTo(fromDate), eqTo(toDate), eqTo(None))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(Forbidden)))

        when(mockRepo.getRecords(eqTo(pstr))).thenReturn(Future.successful(Nil))

        when(mockAppConfig.getAllTransfersYearsOffset).thenReturn(10)

        val result = service.getAllTransfers(pstr).futureValue
        result mustBe Left(UnexpectedError(s"Unable to get all transfers for ${pstr.value}"))
      }

      "merges in-progress (repo) with submitted (downstream) when both present" in {
        val pstr = PstrNumber("24000001AA")

        val ds = DownstreamAllTransfersData(
          DownstreamAllTransfersData.Payload(
            qropsTransferOverview = List(
              DownstreamAllTransfersData.OverviewItem(
                fbNumber                  = "fb",
                qtReference               = "QT123456",
                qtVersion                 = "002",
                qtStatus                  = "Submitted",
                qtDigitalStatus           = "Submitted",
                nino                      = "AA000002A",
                firstName                 = "Alice",
                lastName                  = "Liddell",
                qtDate                    = LocalDate.parse("2025-02-02"),
                qropsReference            = "QROPS9",
                submissionCompilationDate = now
              )
            )
          )
        )

        val inProg = AllTransfersItem(
          transferReference = Some("T-1"),
          qtReference       = None,
          qtVersion         = None,
          nino              = Some("AB123456C"),
          memberFirstName   = Some("In"),
          memberSurname     = Some("Progress"),
          submissionDate    = None,
          lastUpdated       = Some(LocalDate.parse("2025-03-01")),
          qtStatus          = Some(QtStatus("InProgress")),
          pstrNumber        = Some(pstr)
        )

        val toDate: LocalDate   = LocalDate.now(ZoneOffset.UTC)
        val fromDate: LocalDate = toDate.minusYears(10)

        when(mockConnector.getAllTransfers(eqTo(pstr), eqTo(fromDate), eqTo(toDate), eqTo(None))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(ds)))

        when(mockRepo.getRecords(eqTo(pstr))).thenReturn(Future.successful(Seq(inProg)))

        when(mockAppConfig.getAllTransfersYearsOffset).thenReturn(10)

        val result = service.getAllTransfers(pstr).futureValue

        result match {
          case Right(AllTransfersResponse(items)) =>
            items must have size 2
            items.head mustBe inProg
            items(1).qtReference.map(_.value) mustBe Some("QT123456")
          case other => fail(s"Unexpected: $other")
        }
      }

      "returns in-progress items when connector returns NoTransfersFound but repo has records" in {
        val pstr = PstrNumber("24000001AA")

        val inProg = AllTransfersItem(
          transferReference = Some("T-2"),
          qtReference       = None,
          qtVersion         = None,
          nino              = None,
          memberFirstName   = Some("Only"),
          memberSurname     = Some("InProgress"),
          submissionDate    = None,
          lastUpdated       = Some(LocalDate.parse("2025-04-01")),
          qtStatus          = Some(QtStatus("InProgress")),
          pstrNumber        = Some(pstr)
        )

        val toDate: LocalDate   = LocalDate.now(ZoneOffset.UTC)
        val fromDate: LocalDate = toDate.minusYears(10)

        when(mockConnector.getAllTransfers(eqTo(pstr), eqTo(fromDate), eqTo(toDate), eqTo(None))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(NoTransfersFound)))

        when(mockRepo.getRecords(eqTo(pstr))).thenReturn(Future.successful(Seq(inProg)))

        when(mockAppConfig.getAllTransfersYearsOffset).thenReturn(10)

        val result = service.getAllTransfers(pstr).futureValue
        result mustBe Right(AllTransfersResponse(Seq(inProg)))
      }

      "returns in-progress items when connector returns another error but repo has records" in {
        val pstr = PstrNumber("24000001AA")

        val inProg = AllTransfersItem(
          transferReference = Some("T-3"),
          qtReference       = None,
          qtVersion         = None,
          nino              = None,
          memberFirstName   = Some("Still"),
          memberSurname     = Some("Proceeding"),
          submissionDate    = None,
          lastUpdated       = Some(LocalDate.parse("2025-05-01")),
          qtStatus          = Some(QtStatus("InProgress")),
          pstrNumber        = Some(pstr)
        )

        val toDate: LocalDate   = LocalDate.now(ZoneOffset.UTC)
        val fromDate: LocalDate = toDate.minusYears(10)

        when(mockConnector.getAllTransfers(eqTo(pstr), eqTo(fromDate), eqTo(toDate), eqTo(None))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(Unauthorized)))

        when(mockRepo.getRecords(eqTo(pstr))).thenReturn(Future.successful(Seq(inProg)))

        when(mockAppConfig.getAllTransfersYearsOffset).thenReturn(10)

        val result = service.getAllTransfers(pstr).futureValue
        result mustBe Right(AllTransfersResponse(Seq(inProg)))
      }

      "recovers repo failure to empty in-progress and still returns downstream results" in {
        val pstr = PstrNumber("24000001AA")

        val ds = DownstreamAllTransfersData(
          DownstreamAllTransfersData.Payload(
            qropsTransferOverview = List(
              DownstreamAllTransfersData.OverviewItem(
                fbNumber                  = "fb",
                qtReference               = "QT654321",
                qtVersion                 = "001",
                qtStatus                  = "Submitted",
                qtDigitalStatus           = "Submitted",
                nino                      = "AA777777A",
                firstName                 = "Sue",
                lastName                  = "Smith",
                qtDate                    = LocalDate.parse("2025-06-01"),
                qropsReference            = "QROPS777",
                submissionCompilationDate = now
              )
            )
          )
        )

        val toDate: LocalDate   = LocalDate.now(ZoneOffset.UTC)
        val fromDate: LocalDate = toDate.minusYears(10)

        when(mockConnector.getAllTransfers(eqTo(pstr), eqTo(fromDate), eqTo(toDate), eqTo(None))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(ds)))


        when(mockRepo.getRecords(eqTo(pstr))).thenReturn(Future.failed(new RuntimeException("boom")))

        when(mockAppConfig.getAllTransfersYearsOffset).thenReturn(10)

        val result = service.getAllTransfers(pstr).futureValue

        result match {
          case Right(AllTransfersResponse(items)) =>
            items.map(_.qtReference.map(_.value)) mustBe Seq(Some("QT654321"))
          case other => fail(s"Unexpected: $other")
        }
      }
    }
  }
}
