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

package uk.gov.hmrc.overseaspensiontransferbackend.controllers

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.models.PstrNumber
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.GetAllSubmissionsDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission._
import uk.gov.hmrc.overseaspensiontransferbackend.services.SubmissionService

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.Future

class GetAllSubmissionsControllerSpec
    extends AnyFreeSpec
    with Matchers
    with SpecBase {

  private val endpoint     = "/overseas-pension-transfer-backend/get-all-submissions"
  private val fixedInstant = Instant.parse("2025-09-16T12:00:00Z")
  private val fixedClock   = Clock.fixed(fixedInstant, ZoneOffset.UTC)

  "GetAllSubmissionsController.getAllSubmissions" - {

    "must return 200 with DTO JSON when PSTR is valid and submissions exist" in {
      val mockSubmissionService: SubmissionService = mock[SubmissionService]
      val pstrStr                                  = "24000001AA"
      val pstr                                     = PstrNumber(pstrStr)

      val items: Seq[SubmissionGetAllItem] =
        Seq(SubmissionGetAllItem(
          transferReference = None,
          qtReference       = None,
          nino              = None,
          memberFirstName   = None,
          memberSurname     = None,
          submissionDate    = None,
          qtStatus          = None,
          schemeId          = Some(pstr)
        ))

      when(
        mockSubmissionService.getAllSubmissions(eqTo(pstr))(
          any[HeaderCarrier]
        )
      ).thenReturn(Future.successful(Right(SubmissionGetAllResponse(Some(items)))))

      val app: Application =
        applicationBuilder()
          .overrides(
            bind[SubmissionService].toInstance(mockSubmissionService),
            bind[Clock].toInstance(fixedClock)
          )
          .build()

      running(app) {
        val request = FakeRequest(GET, s"$endpoint/$pstrStr")
        val result  = route(app, request).value

        status(result) mustBe OK

        val expectedDto = GetAllSubmissionsDTO.from(pstr, items)(fixedClock)
        contentAsJson(result) mustBe Json.toJson(expectedDto)
      }
    }

    "must return 404 when PSTR is valid but there are no submissions (None from upstream)" in {
      val mockSubmissionService: SubmissionService = mock[SubmissionService]
      val pstrStr                                  = "24000001AA"
      val pstr                                     = PstrNumber(pstrStr)

      when(mockSubmissionService.getAllSubmissions(eqTo(pstr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(SubmissionGetAllResponse(None))))

      val app: Application =
        applicationBuilder()
          .overrides(
            bind[SubmissionService].toInstance(mockSubmissionService),
            bind[Clock].toInstance(fixedClock)
          )
          .build()

      running(app) {
        val request = FakeRequest(GET, s"$endpoint/$pstrStr")
        val result  = route(app, request).value

        status(result) mustBe NOT_FOUND
      }
    }

    "must return 400 when PSTR is invalid" in {
      val app: Application =
        applicationBuilder()
          .overrides(bind[Clock].toInstance(fixedClock))
          .build()

      running(app) {
        val badPstr = "not-a-pstr"
        val request = FakeRequest(GET, s"$endpoint/$badPstr")
        val result  = route(app, request).value

        status(result)        mustBe BAD_REQUEST
        contentAsString(result) must include("PSTR must be 8 digits followed by 2 letters")
      }
    }

    // TODO: Test the Left(error) branch from SubmissionService
  }
}
