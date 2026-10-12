/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.overseaspensiontransferbackend.validators

import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError

import scala.concurrent.Future

object PathParameterValidator extends Logging {
  private val qtNumberPattern   = "^QT[0-9]{6}$".r
  private val pstrNumberPattern = "^[0-9]{8}[A-Z]{2}$".r

  private def normalise(s: String): String =
    s.replaceAll("\\s+", "").toUpperCase

  def isQtNumber(value: String): Boolean = qtNumberPattern.matches(value)

  def isPstrNumber(value: String): Boolean = pstrNumberPattern.matches(normalise(value))
  
  def withPstrNumber(pstr: String)(block: String => Future[Result]) : Future[Result] =
    if (isPstrNumber(pstr)) {
      block(pstr)
    } else {
      logger.error("PstrNumber has an invalid format. PstrNumber: " + pstr)
      Future.successful(InternalServerError(Json.obj(
        "error" -> "Invalid Pstr",
        "details" -> pstr
      )))
    }
}
