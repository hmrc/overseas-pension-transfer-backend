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

package uk.gov.hmrc.overseaspensiontransferbackend.models.authentication

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.overseaspensiontransferbackend.models.{PensionSchemeDetails, PstrNumber, SrnNumber}

class AuthenticatedUserSpec extends AnyFreeSpec with Matchers {

  private val testPensionSchemeDetails: PensionSchemeDetails = PensionSchemeDetails(
    SrnNumber("S1234567"),
    PstrNumber("1202383AB"),
    "Scheme Name"
  )

  "PsaUser" - {
    "userType" - {
      "must return Psa" in {
        PsaUser(PsaId("A0000001"), "id", None, affinityGroup = Individual).userType mustBe Psa
      }
    }

    "updatePensionSchemeDetails" - {
      "must add PensionSchemeDetails when PsaUser.pensionSchemeDetails = None" in {
        PsaUser(PsaId("S1234567"), "id", None, affinityGroup = Individual).updatePensionSchemeDetails(testPensionSchemeDetails) mustBe
          PsaUser(PsaId("S1234567"), "id", Some(testPensionSchemeDetails), affinityGroup = Individual)
      }

      "must update with PensionSchemeDetails PsaUser.pensionSchemeDetails = Some(details)" in {
        val existingDetails = PensionSchemeDetails(
          SrnNumber("S7654321"),
          PstrNumber("111111111ZY"),
          "Pensions For Software Engineers"
        )

        PsaUser(PsaId("S1234567"), "id", Some(existingDetails), affinityGroup = Individual).updatePensionSchemeDetails(testPensionSchemeDetails) mustBe
          PsaUser(PsaId("S1234567"), "id", Some(testPensionSchemeDetails), affinityGroup = Individual)
      }
    }
  }

  "PspUser" - {
    "userType" - {
      "must return Psp" in {
        PspUser(PspId("A0000001"), "id", None, affinityGroup = Individual).userType mustBe Psp
      }
    }

    "updatePensionSchemeDetails" - {
      "must add PensionSchemeDetails when PsaUser.pensionSchemeDetails = None" in {
        PspUser(PspId("S1234567"), "id", None, affinityGroup = Individual).updatePensionSchemeDetails(testPensionSchemeDetails) mustBe
          PspUser(PspId("S1234567"), "id", Some(testPensionSchemeDetails), affinityGroup = Individual)
      }

      "must update with PensionSchemeDetails PsaUser.pensionSchemeDetails = Some(details)" in {
        val existingDetails = PensionSchemeDetails(
          SrnNumber("S7654321"),
          PstrNumber("111111111ZY"),
          "Pensions For Software Engineers"
        )

        PspUser(PspId("S1234567"), "id", Some(existingDetails), affinityGroup = Individual).updatePensionSchemeDetails(testPensionSchemeDetails) mustBe
          PspUser(PspId("S1234567"), "id", Some(testPensionSchemeDetails), affinityGroup = Individual)
      }
    }
  }

}
