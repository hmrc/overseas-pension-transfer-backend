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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers.aboutReceivingQROPS

import play.api.libs.json.{JsError, JsObject, JsPath}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.NameTransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.Transformer

class QropsSchemeManagerOrganisationContactNameTransformer extends Transformer with NameTransformerStep {
  override val externalPath: JsPath = JsPath \ "schemeManagerDetails" \ "organisationContactName"
  override val internalPath: JsPath = JsPath \ "aboutReceivingQROPS" \ "qropsSchemeManagerType" \ "qropsOrganisation"

  override def construct(input: JsObject): Either[JsError, JsObject] = {
    val steps: Seq[TransformerStep] = Seq(
      moveStep(externalPath, internalPath \ "name"),
      flattenName(
        internalPath \ "name",
        "orgForename",
        "orgSurname"
      )
    )
    applyPipeline(input, steps)(identity)
  }

  override val deconstructor: Seq[TransformerStep] = Seq(deconstructName(
    "orgForename",
    "orgSurname",
    internalPath
  ))
}
