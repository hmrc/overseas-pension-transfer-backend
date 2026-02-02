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

package uk.gov.hmrc.overseaspensiontransferbackend.base

import org.mockito.ArgumentMatchers
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.controllers.actions.{FakeIdentifierAction, IdentifierAction}
import uk.gov.hmrc.overseaspensiontransferbackend.models.*
import uk.gov.hmrc.overseaspensiontransferbackend.models.audit.AuditUserInfo
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication.{AuthenticatedUser, Psa, PsaId, PsaUser, Psp, PspId, PspUser}
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.requests.IdentifierRequest
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.TransferNumber
import uk.gov.hmrc.overseaspensiontransferbackend.validators.Submission

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext

trait SpecBase
    extends Matchers
    with TryValues
    with OptionValues
    with EitherValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar {

  implicit lazy val hc: HeaderCarrier    = HeaderCarrier()
  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val mockAppConfig: AppConfig  = mock[AppConfig]

  val testId: TransferNumber = TransferNumber(UUID.randomUUID().toString)
  val pstr: PstrNumber       = PstrNumber("12345678AB")
  val now: Instant           = Instant.parse("2025-04-11T12:00:00Z")

  val sampleAnswersData: AnswersData = AnswersData(
    transferringMember  = None,
    aboutReceivingQROPS = None,
    transferDetails     = None,
    submitToHMRC        = Some(true)
  )

  val samplePsaSubmission: Submission = Submission(
    ReportDetails(pstr.value, Submitted, None, None),
    None,
    None,
    None,
    QtDeclaration(
      Psa,
      PsaId("A1234567"),
      None
    ),
    Some(Declaration(declaration1 = true, declaration2 = true)),
    None
  )

  val samplePspSubmission: Submission = Submission(
    ReportDetails(pstr.value, Submitted, None, None),
    None,
    None,
    None,
    QtDeclaration(
      Psp,
      PspId("12345678"),
      Some(PsaId("A1234567"))
    ),
    None,
    Some(Declaration(declaration1 = true, declaration2 = true))
  )

  val simpleSavedUserAnswers: SavedUserAnswers = SavedUserAnswers(
    transferId  = testId,
    pstr        = pstr,
    data        = sampleAnswersData,
    lastUpdated = now
  )

  val simpleUserAnswersDTO: UserAnswersDTO = UserAnswersDTO(
    transferId  = testId,
    pstr        = pstr,
    data        = Json.obj("someField" -> "someIncomingValue"),
    lastUpdated = now
  )

  val psaId: PsaId = PsaId("A123456")

  val psaUser: PsaUser = PsaUser(psaId, internalId = "id", affinityGroup = Individual)

  val pspId: PspId = PspId("A123456")

  val pspUser: PspUser = PspUser(pspId, internalId = "id", affinityGroup = Individual)

  val schemeDetails = PensionSchemeDetails(
    SrnNumber("S1234567"),
    PstrNumber("12345678AB"),
    "SchemeName"
  )

  def quotedShare(v: Int, n: Int, c: String, cls: String): JsObject =
    Json.obj("quotedValue" -> BigDecimal(v), "quotedShareTotal" -> n, "quotedCompany" -> c, "quotedClass" -> cls)

  def unquotedShare(v: Int, n: Int, c: String, cls: String): JsObject =
    Json.obj("unquotedValue" -> BigDecimal(v), "unquotedShareTotal" -> n, "unquotedCompany" -> c, "unquotedClass" -> cls)

  protected def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder().overrides(
      bind[IdentifierAction].to[FakeIdentifierAction]
    )

  def fakeIdentifierRequest[A](
      fakeRequest: FakeRequest[A],
      authenticatedUser: AuthenticatedUser = psaUser.updatePensionSchemeDetails(schemeDetails)
    ): IdentifierRequest[A] =
    IdentifierRequest(fakeRequest, authenticatedUser)

  implicit val testIdentifierRequest: IdentifierRequest[_] =
    IdentifierRequest(FakeRequest(), psaUser.updatePensionSchemeDetails(schemeDetails))

  val sampleAuditUserInfoPsa = AuditUserInfo(Psa, AffinityGroup.Individual, psaId, None)
  val sampleAuditUserInfoPsp = AuditUserInfo(Psp, AffinityGroup.Individual, pspId, Some(psaId))

  val correlationId = "e470d658-99f7-4292-a4a1-ed12c72f1337"
}
