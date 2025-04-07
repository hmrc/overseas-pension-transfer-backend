package uk.gov.hmrc.overseaspensiontransferbackend.controllers

import org.mockito.ArgumentMatchersSugar
import org.mockito.MockitoSugar
import org.mockito.captor.ArgCaptor
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.FakeRequest
import uk.gov.hmrc.overseaspensiontransferbackend.models.UserAnswers
import uk.gov.hmrc.overseaspensiontransferbackend.services.CompileAndSubmitService
import play.api.inject.bind
import play.api.Application
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase

import java.time.Instant
import scala.concurrent.Future

class UserAnswersControllerSpec
    extends SpecBase
    with ArgumentMatchersSugar
    with MockitoSugar {



  private val testId = "test-id"
  private val now: Instant = Instant.parse("2025-04-11T12:00:00Z")

  "UserAnswersController.getAnswers" - {

    "return 200 (OK) and the JSON if the service returns Some(UserAnswers)" in {

      val mockCompileAndSubmitService = mock[CompileAndSubmitService]

      val userAnswers = UserAnswers(testId, Json.obj("someField" -> "someValue"), now)

      when(mockCompileAndSubmitService.getAnswers(testId))
        .thenReturn(Future.successful(Some(userAnswers)))

      val application: Application =
        applicationBuilder()
          .overrides(
            bind[CompileAndSubmitService].toInstance(mockCompileAndSubmitService)
          )
          .build()

      running(application) {
        val request  = FakeRequest(GET, s"/user-answers/$testId")
        val result   = route(application, request).value

        status(result)          mustEqual OK
        contentAsJson(result)   mustEqual Json.toJson(userAnswers)
        verify(mockCompileAndSubmitService).getAnswers(testId)
      }
    }

    "return 404 (NOT_FOUND) if the service returns None" in {
      val mockCompileAndSubmitService = mock[CompileAndSubmitService]

      when(mockCompileAndSubmitService.getAnswers(eqTo(testId)))
        .thenReturn(Future.successful(None))

      val application: Application =
        applicationBuilder()
          .overrides(
            bind[CompileAndSubmitService].toInstance(mockCompileAndSubmitService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, s"/user-answers/$testId")
        val result  = route(application, request).value

        status(result) mustEqual NOT_FOUND
        verify(mockCompileAndSubmitService).getAnswers(eqTo(testId))
      }
    }
  }

  "UserAnswersController.putAnswers" - {

    "return 200 (OK) and the updated JSON if JSON parsing and upsert succeed" in {
      val mockCompileAndSubmitService = mock[CompileAndSubmitService]
      val incomingJson = Json.obj(
        "id"          -> "ignored-in-request-body",
        "data"        -> Json.obj("someField" -> "someIncomingValue"),
        "lastUpdated" -> "2025-04-11T12:00:00Z"
      )

      val userAnswers = UserAnswers(
        id          = testId,
        data        = Json.obj("someField" -> "someIncomingValue"),
        lastUpdated = now
      )

      when(mockCompileAndSubmitService.upsertAnswers(any[UserAnswers]))
        .thenReturn(Future.successful(userAnswers))

      val application: Application =
        applicationBuilder()
          .overrides(
            bind[CompileAndSubmitService].toInstance(mockCompileAndSubmitService)
          )
          .build()

      running(application) {
        val request = FakeRequest(PUT, s"/user-answers/$testId").withJsonBody(incomingJson)
        val result  = route(application, request).value

        status(result)        mustEqual OK
        contentAsJson(result) mustEqual Json.toJson(userAnswers)

        val captor = ArgCaptor[UserAnswers]
        verify(mockCompileAndSubmitService).upsertAnswers(captor)
        captor.value.id          mustEqual testId
        captor.value.lastUpdated mustEqual now
      }
    }

    "return 400 (BAD_REQUEST) if JSON validation fails" in {
      val mockCompileAndSubmitService = mock[CompileAndSubmitService]

      val badJson = Json.obj("id" -> testId) // missing data, lastUpdated

      val application: Application =
        applicationBuilder()
          .overrides(
            bind[CompileAndSubmitService].toInstance(mockCompileAndSubmitService)
          )
          .build()

      running(application) {
        val request = FakeRequest(PUT, s"/user-answers/$testId").withJsonBody(badJson)
        val result  = route(application, request).value

        status(result)            mustEqual BAD_REQUEST
        contentAsString(result)   must include("Invalid JSON")

        verifyNoMoreInteractions(mockCompileAndSubmitService)
      }
    }
  }
}
