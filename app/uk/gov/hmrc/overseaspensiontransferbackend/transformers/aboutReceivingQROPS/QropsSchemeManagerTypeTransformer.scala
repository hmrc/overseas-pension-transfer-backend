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

import play.api.libs.json.{JsPath, JsString}
import uk.gov.hmrc.overseaspensiontransferbackend.models.SchemeManagerType
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.EnumTransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.Transformer

class QropsSchemeManagerTypeTransformer extends Transformer with EnumTransformerStep {
  override val jsonKey              = "schemeManagerType"
  override val externalPath: JsPath = JsPath \ "schemeManagerDetails"
  override val internalPath: JsPath = JsPath \ "aboutReceivingQROPS" \ "qropsSchemeManagerType"

  override val constructor: Seq[TransformerStep] = {
    val enumConversion: SchemeManagerType => JsString = schemeManagerType => JsString(schemeManagerType.downstreamValue)
    Seq(constructEnum[SchemeManagerType](internalPath \ jsonKey, enumConversion))
  }

  override val deconstructor: Seq[TransformerStep] = {
    val stringConversion: String => SchemeManagerType = string => SchemeManagerType(string)
    Seq(deconstructEnum[SchemeManagerType](internalPath, stringConversion))
  }
}
