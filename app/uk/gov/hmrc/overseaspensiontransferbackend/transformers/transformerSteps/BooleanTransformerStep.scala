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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

trait BooleanTransformerStep extends JsonHelpers {

  /** Converts a boolean at the given path into a Yes/No string */
  def constructBool(path: JsPath): JsObject => Either[JsError, JsObject] = { json =>
    path.asSingleJson(json).validate[Boolean] match {
      case JsSuccess(value, _) =>
        val stringValue = JsString(if (value) "Yes" else "No")
        setPath(path, stringValue, json)
      case _: JsError          =>
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
