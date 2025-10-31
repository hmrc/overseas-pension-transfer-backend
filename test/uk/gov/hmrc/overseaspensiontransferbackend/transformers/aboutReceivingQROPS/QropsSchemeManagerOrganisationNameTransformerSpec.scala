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

class QropsSchemeManagerOrganisationNameTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer = new QropsSchemeManagerOrganisationNameTransformer

  "QropsSchemeManagerOrganisationNameTransformer" - {

    "must construct the correct structure with flattened name fields" in {
      val inputJson = Json.obj(
        "schemeManagerDetails" -> Json.obj(
          "orgName" -> "Organisation"
        )
      )

      val expected = Json.obj(
        "aboutReceivingQROPS" -> Json.obj(
          "qropsSchemeManagerType" -> Json.obj(
            "qropsOrganisation" -> Json.obj(
              "orgName" -> "Organisation"
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
              "orgName" -> "Organisation"
            )
          )
        )
      )

      val expected = Json.obj(
        "schemeManagerDetails" -> Json.obj(
          "orgName" -> "Organisation"
        )
      )

      transformer.deconstruct(inputJson) mustBe Right(expected)
    }

    "must be added to qropsOrganisation when orgForename and orgSurname are already populated" in {
      val existingJson = Json.obj("aboutReceivingQROPS" -> Json.obj(
        "qropsSchemeManagerType" -> Json.obj(
          "qropsOrganisation" -> Json.obj(
            "orgForename" -> "Org",
            "orgSurname"  -> "Surname"
          )
        )
      ))

      val inputJson = Json.obj(
        "schemeManagerDetails" -> Json.obj(
          "orgName" -> "Organisation"
        )
      )

      val mergedInput = existingJson.deepMerge(inputJson)

      val expected = Json.obj("aboutReceivingQROPS" -> Json.obj(
        "qropsSchemeManagerType" -> Json.obj(
          "qropsOrganisation" -> Json.obj(
            "orgForename" -> "Org",
            "orgSurname"  -> "Surname",
            "orgName"     -> "Organisation"
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
      val inputJson = Json.obj("schemeManagerDetails" -> Json.obj())

      transformer.deconstruct(inputJson) mustBe Right(inputJson)
    }
  }
}
