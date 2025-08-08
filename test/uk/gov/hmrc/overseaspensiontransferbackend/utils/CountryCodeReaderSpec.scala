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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.models.Country

class CountryCodeReaderSpec extends AnyFreeSpec with Matchers with SpecBase {

  private val reader = applicationBuilder().injector().instanceOf[CountryCodeReader]

  "CountryCodeReader" - {
    "return Some(country) when code is present in file" in {
      reader.readCountryCode("GB") mustBe Country("GB", Some("United Kingdom"))
    }

    "return None when code is not present in file" in {
      reader.readCountryCode("12") mustBe Country("12", None)
    }
  }
}
