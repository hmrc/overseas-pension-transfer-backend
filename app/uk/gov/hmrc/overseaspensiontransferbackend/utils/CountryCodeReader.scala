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

package uk.gov.hmrc.overseaspensiontransferbackend.utils

import com.google.inject.Inject
import play.api.Environment
import uk.gov.hmrc.overseaspensiontransferbackend.models.Country

import scala.io.Source

class CountryCodeReader @Inject() (env: Environment) {

  def readCountryCode(code: String): Country = {
    env.resourceAsStream("CountryCodes.csv") match {
      case Some(value) =>
        val mappedCodes: Map[String, String] = Source.fromInputStream(value).getLines().map {
          csv =>
            csv.split(',')(0) -> csv.split(',')(1)
        }.toMap

        Country(code, mappedCodes.get(code))

      case None => throw new RuntimeException("Someone deleted the .csv file. Please put it back")
    }
  }

}
