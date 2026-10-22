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

import com.google.inject.Inject
import play.api.libs.json.JsPath
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.AddressTransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.Transformer
import uk.gov.hmrc.overseaspensiontransferbackend.utils.{CountryCodeReader, JsonHelpers}

class QropsAddressTransformer @Inject() (val countryCodeReader: CountryCodeReader) extends Transformer with AddressTransformerStep with JsonHelpers {

  override val jsonKey                             = "receivingQropsAddress"
  override val externalPath: JsPath                = JsPath \ "qropsDetails"
  override val internalPath: JsPath                = JsPath \ "aboutReceivingQROPS"
  override val constructor: Seq[TransformerStep]   = Seq(constructAddressAt(internalPath \ jsonKey))
  override val deconstructor: Seq[TransformerStep] = Seq(deconstructAddressAt(internalPath \ jsonKey))

}
