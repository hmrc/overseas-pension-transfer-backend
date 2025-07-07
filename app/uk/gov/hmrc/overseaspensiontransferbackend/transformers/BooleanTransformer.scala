package uk.gov.hmrc.overseaspensiontransferbackend.transformers

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

trait BooleanTransformer extends JsonHelpers {

  /** Converts a boolean at the given path into a Yes/No string */
  def constructBool(path: JsPath): JsObject => Either[JsError, JsObject] = { json =>
    path.asSingleJson(json).validate[Boolean] match {
      case JsSuccess(value, _) =>
        val stringValue = JsString(if (value) "Yes" else "No")
        setPath(path, stringValue, json)
      case _: JsError =>
        Right(json)
    }
  }

  /** Converts a Yes/No string at the given path into a boolean */
  def deconstructBool(path: JsPath): JsObject => Either[JsError, JsObject] = { json =>
    path.asSingleJson(json).validate[String] match {
      case JsSuccess("Yes", _) => setPath(path, JsBoolean(true), json)
      case JsSuccess("No", _)  => setPath(path, JsBoolean(false), json)
      case _                   => Right(json)
    }
  }
}
