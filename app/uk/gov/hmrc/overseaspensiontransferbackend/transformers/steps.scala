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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.utils.JsonHelpers

package object steps extends JsonHelpers {
  type TransformerStep = JsObject => Either[JsError, JsObject]

  def moveStep(from: JsPath, to: JsPath): TransformerStep =
    (json: JsObject) => movePath(from, to, json)

  /** This function conditionally prunes an internal JSON key if the *external* key is being set.
    *
    * For example, if the frontend sends `qropsEstablishedCountry`, then the internal `qropsEstablishedOther` field should be removed if it exists, to avoid
    * conflicting data.
    *
    * Usage: conditionalPruneStep( onlyIfSetAt = qropsEstablishedCountryPath, // this is the external (frontend) path pruneTarget = qropsEstablishedOtherPath //
    * this is the internal path to remove )
    */
  def conditionalPruneStep(onlyIfSetAt: JsPath, pruneTarget: JsPath): TransformerStep =
    (json: JsObject) => {
      val isSet = onlyIfSetAt.asSingleJson(json) match {
        case JsDefined(JsNull) => false
        case JsDefined(_)      => true
        case _                 => false
      }

      if (isSet) {
        Right(prunePath(pruneTarget)(json))
      } else {
        Right(json)
      }
    }

}
