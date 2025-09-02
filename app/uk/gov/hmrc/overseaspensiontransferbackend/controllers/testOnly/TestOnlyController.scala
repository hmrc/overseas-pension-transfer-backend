package uk.gov.hmrc.overseaspensiontransferbackend.controllers.testOnly

import com.google.inject.Inject
import play.api.mvc.Result
import play.api.mvc.Results.NoContent
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository

import scala.concurrent.{ExecutionContext, Future}

class TestOnlyController @Inject()(repository: SaveForLaterRepository)(implicit ec: ExecutionContext) {

  def resetDatabase: Future[Result] = {
    repository.clear.map(_ => NoContent)
  }
}
