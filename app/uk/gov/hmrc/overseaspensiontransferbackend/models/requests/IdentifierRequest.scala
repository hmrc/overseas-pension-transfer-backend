package uk.gov.hmrc.overseaspensiontransferbackend.models.requests


import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication.AuthenticatedUser

final case class IdentifierRequest[A](request: Request[A], authenticatedUser: AuthenticatedUser)
  extends WrappedRequest[A](request)
