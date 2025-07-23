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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class QropsSchemeManagerorganisationContactNameTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer = new QropsSchemeManagerOrganisationContactNameTransformer

  "QropsSchemeManagerOrganisationContactNameTransformer" - {

    "must construct the correct structure with flattened name fields" in {
      val inputJson = Json.obj(
        "schemeManagerDetails" -> Json.obj(
          "organisationContactName" -> Json.obj(
            "firstName" -> "Mathew",
            "lastName"  -> "May"
          )
        )
      )

      val expected = Json.obj(
        "aboutReceivingQROPS" -> Json.obj(
          "qropsSchemeManagerType" -> Json.obj(
            "qropsOrganisation" -> Json.obj(
              "orgForename" -> "Mathew",
              "orgSurname"  -> "May"
            )
          )
        )
      )

      transformer.construct(inputJson) mustBe Right(expected)
    }

    "must deconstruct the flattened structure back to nested name fields" in {
      val inputJson = Json.obj(
        "aboutReceivingQROPS" -> Json.obj(
          "qropsSchemeManagerType" -> Json.obj(
            "qropsOrganisation" -> Json.obj(
              "orgForename" -> "Mathew",
              "orgSurname"  -> "May"
            )
          )
        )
      )

      val expected = Json.obj(
        "schemeManagerDetails" -> Json.obj(
          "organisationContactName" -> Json.obj(
            "firstName" -> "Mathew",
            "lastName"  -> "May"
          )
        )
      )

      transformer.deconstruct(inputJson) mustBe Right(expected)
    }

    "must remove qropsOrganisation if already exists in Json path" in {
      val existingJson = Json.obj("aboutReceivingQROPS" -> Json.obj(
        "qropsSchemeManagerType" -> Json.obj(
          "qropsIndividual" -> Json.obj(
            "individualForename" -> "first name",
            "individualSurname"  -> "surname"
          )
        )
      ))

      val inputJson = Json.obj(
        "schemeManagerDetails" -> Json.obj(
          "organisationContactName" -> Json.obj(
            "firstName" -> "Organisation",
            "lastName"  -> "Surname"
          )
        )
      )

      val mergedInput = existingJson.deepMerge(inputJson)

      val expected = Json.obj("aboutReceivingQROPS" -> Json.obj(
        "qropsSchemeManagerType" -> Json.obj(
          "qropsOrganisation" -> Json.obj(
            "orgForename" -> "Organisation",
            "orgSurname"  -> "Surname"
          )
        )
      ))

      transformer.construct(mergedInput) mustBe Right(expected)
    }

    "must return original JSON if name key not present on construct" in {
      val inputJson = Json.obj("aboutReceivingQROPS" -> Json.obj("qropsSchemeManagerType" -> Json.obj("qropsOrganisation" -> Json.obj())))

      transformer.construct(inputJson) mustBe Right(inputJson)
    }

    "must return original JSON if foreName/lastName not present on deconstruct" in {
      val inputJson = Json.obj("schemeManagerDetails" -> Json.obj("organisationContactName" -> Json.obj()))

      transformer.deconstruct(inputJson) mustBe Right(inputJson)
    }
  }
}
