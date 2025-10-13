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

///*
// * Copyright 2025 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.overseaspensiontransferbackend.services
//
//import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
//import org.mockito.{ArgumentCaptor, Mockito}
//import org.mockito.ArgumentMatchers.any
//import org.mockito.Mockito.{times, verify, when}
//import org.scalatest.BeforeAndAfterEach
//import org.scalatest.freespec.AnyFreeSpec
//import org.scalatest.matchers.must.Matchers
//import org.scalatestplus.mockito.MockitoSugar
//import play.api.libs.json.Json
//import uk.gov.hmrc.http.HeaderCarrier
//import uk.gov.hmrc.overseaspensiontransferbackend.models.audit.{JourneySubmittedType, ReportSubmittedAuditModel}
//import uk.gov.hmrc.play.audit.http.connector.AuditConnector
//import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
//
//import scala.concurrent.ExecutionContext.Implicits.global
//
//class AuditServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar with BeforeAndAfterEach {
//  private val mockAuditConnector: AuditConnector    = mock[AuditConnector]
//  private val mockAppConfig                         = mock[AppConfig]
//  implicit private val headerCarrier: HeaderCarrier = HeaderCarrier()
//  private val appName                               = "audit-source"
//
//  private val service = new AuditService(mockAppConfig, mockAuditConnector)
//
//  override def beforeEach(): Unit = {
//    Mockito.reset(mockAuditConnector)
//    Mockito.reset(mockAppConfig)
//    super.beforeEach()
//  }
//
//  ".sendAudit" - {
//    JourneySubmittedType.values.foreach {
//      journey =>
//        s"must send an event to the audit connector for $journey event type" in {
//          val eventModel = ReportSubmittedAuditModel("test values")
//
//          when(mockAppConfig.appName).thenReturn(appName)
//          service.audit(eventModel)
//          val eventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])
//
//          verify(mockAuditConnector, times(1)).sendExtendedEvent(eventCaptor.capture())(any(), any())
//
//          val expectedJson = Json.obj(
//            "journey"                   -> journey.toString,
//            "internalReportReferenceId" -> "testID",
//            "roleLoggedInAs"            -> "Psa",
//            "affinityGroup"             -> "Individual",
//            "requesterIdentifier"       -> "21000005"
//          )
//
//          val event = eventCaptor.getValue
//          event.auditSource mustEqual appName
//          event.auditType mustEqual "OverseasPensionTransferReportStarted"
//          event.detail mustEqual expectedJson
//        }
//    }
//  }
//}
