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
import play.api.libs.json.*
import uk.gov.hmrc.overseaspensiontransferbackend.models.Country
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transformerSteps.EnumTransformerStep
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.Transformer
import uk.gov.hmrc.overseaspensiontransferbackend.utils.{CountryCodeReader, JsonHelpers}

class QropsEstablishedCountryTransformer @Inject() (countryCodeReader: CountryCodeReader) extends Transformer with EnumTransformerStep
    with JsonHelpers {

  override val jsonKey              = "qropsEstablished"
  override val externalPath: JsPath = JsPath \ "qropsDetails"
  override val internalPath: JsPath = JsPath \ "aboutReceivingQROPS" \ "receivingQropsEstablishedDetails"

  /** Applies a transformation from raw frontend input (e.g. UserAnswersDTO.data) into the correct internal shape for AnswersData.
    *
    * If the structured country is being set by the frontend, this removes any conflicting 'other' text-based value.
    */
  override val constructor: Seq[TransformerStep] = {
    val enumFromCountry: Country => JsValue = country => JsString(country.code)
    Seq(constructEnum[Country](internalPath \ jsonKey, enumFromCountry))
  }

  override val deconstructor: Seq[TransformerStep] = {
    val enumFromString: String => JsValue = string => Json.toJson(countryCodeReader.readCountryCode(string))
    Seq(constructEnum[String](internalPath \ jsonKey, enumFromString))
  }
}
