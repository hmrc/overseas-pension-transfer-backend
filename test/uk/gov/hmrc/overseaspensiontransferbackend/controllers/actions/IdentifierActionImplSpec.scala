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

package uk.gov.hmrc.overseaspensiontransferbackend.controllers.actions

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results.Ok
import play.api.mvc.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.*
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models.requests.IdentifierRequest

import scala.concurrent.{ExecutionContext, Future}

class IdentifierActionImplSpec extends AnyFreeSpec with SpecBase {
  implicit override lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val application = new GuiceApplicationBuilder().build()

  private val bodyParsers       = application.injector.instanceOf[BodyParsers.Default]
  private val appConfig         = application.injector.instanceOf[AppConfig]
  private val mockAuthConnector = mock[AuthConnector]

  private val internalIdValue = "test-user-id"
  private val enrolmentKey    = "HMRC-PODS-ORG"
  private val affinityGroup   = Individual
  private val identifierKey   = "PSAID"
  private val identifierValue = "A1234567"

  private val enrolment = Enrolment(
    enrolmentKey,
    Seq(EnrolmentIdentifier(identifierKey, identifierValue)),
    "Activated"
  )

  type RetrievalResult = Option[String] ~ Enrolments ~ Option[AffinityGroup]

  private val action = new IdentifierActionImpl(mockAuthConnector, appConfig, bodyParsers)

  private val fakeRequest = FakeRequest()

  private def stubAuthoriseReturns(value: RetrievalResult): Unit =
    when(
      mockAuthConnector.authorise[RetrievalResult](
        any[Predicate],
        any[Retrieval[RetrievalResult]]
      )(
        any[HeaderCarrier],
        any[ExecutionContext]
      )
    ).thenReturn(Future.successful(value))

  private def stubAuthoriseFails(ex: Throwable): Unit =
    when(
      mockAuthConnector.authorise[RetrievalResult](
        any[Predicate],
        any[Retrieval[RetrievalResult]]
      )(
        any[HeaderCarrier],
        any[ExecutionContext]
      )
    ).thenReturn(Future.failed(ex))

  "IdentifierAction" - {

    "must allow access with valid internalId, required enrolment and affinityGroup" in {
      val expectedRetrieval = Some(internalIdValue) and Enrolments(Set(enrolment)) and Some(affinityGroup)

      stubAuthoriseReturns(expectedRetrieval)

      val result = action.invokeBlock(
        fakeRequest,
        { (_: IdentifierRequest[AnyContent]) =>
          Future.successful(Ok(s"OK"))
        }
      )

      status(result) mustBe OK
    }

    "must not allow access without affinityGroup" in {
      val expectedRetrieval = Some(internalIdValue) and Enrolments(Set(enrolment)) and None

      stubAuthoriseReturns(expectedRetrieval)

      val result = action.invokeBlock(
        fakeRequest,
        { (request: IdentifierRequest[AnyContent]) =>
          Future.successful(Ok(s"OK - ${request.authenticatedUser.internalId} - ${request.authenticatedUser}"))
        }
      )

      status(result) mustBe UNAUTHORIZED
    }

    "must return internal server error result if no active session" in {
      stubAuthoriseFails(new BearerTokenExpired)

      val result = action.invokeBlock(fakeRequest, (_: IdentifierRequest[AnyContent]) => fail("Should not reach block"))

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

    "must not allow access on InsufficientEnrolments" in {
      stubAuthoriseFails(new InsufficientEnrolments)

      val result = action.invokeBlock(fakeRequest, (_: IdentifierRequest[AnyContent]) => fail("Should not reach block"))

      status(result) mustBe UNAUTHORIZED
    }

    "must not allow access on unexpected error" in {
      stubAuthoriseFails(new RuntimeException("Unexpected error"))

      val result = action.invokeBlock(fakeRequest, (_: IdentifierRequest[AnyContent]) => fail("Should not reach block"))

      status(result) mustBe UNAUTHORIZED
    }

    "must not allow access for users with Agent affinity group" in {
      val expectedRetrieval = Some(internalIdValue) and Enrolments(Set(enrolment)) and Some(AffinityGroup.Agent)
      stubAuthoriseReturns(expectedRetrieval)

      val result = action.invokeBlock(
        fakeRequest,
        (_: IdentifierRequest[AnyContent]) => fail("Should not reach block")
      )

      status(result) mustBe UNAUTHORIZED
    }
  }
}
