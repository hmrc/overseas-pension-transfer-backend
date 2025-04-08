package uk.gov.hmrc.overseaspensiontransferbackend.connectors

import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.overseaspensiontransferbackend.config.AppConfig
import uk.gov.hmrc.overseaspensiontransferbackend.models.UserAnswers

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HttpReadsInstances.readEitherOf

trait CompileAndSubmitConnector {
  def getAnswers(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[UpstreamErrorResponse, UserAnswers]]
  def upsertAnswers(answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UserAnswers]
}

@Singleton()
class CompileAndSubmitStubConnectorImpl @Inject()(httpClient: HttpClientV2, appConfig: AppConfig)
  extends CompileAndSubmitConnector {

  private val stubsBaseUrl: String = appConfig.stubs

  private def stubUrl(id: String): URL =
    url"$stubsBaseUrl/stub-answers/$id"

  override def getAnswers(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext)
  : Future[Either[UpstreamErrorResponse, UserAnswers]] = {
    httpClient
      .get(stubUrl(id))
      .execute[Either[UpstreamErrorResponse, UserAnswers]]
  }

  override def upsertAnswers(answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext)
  : Future[UserAnswers] = {
    httpClient
      .put(stubUrl(answers.id))
      .withBody(Json.toJson(answers))
      .execute[UserAnswers]
  }
}