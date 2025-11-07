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

package uk.gov.hmrc.overseaspensiontransferbackend.models.dtos

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.models.authentication.{Psa, PsaId, Psp, PspId}
import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer._

import java.time.Instant

class SubmissionDTOSpec extends AnyFreeSpec with SpecBase {

  private val ts = Instant.parse("2025-08-01T12:00:00Z")

  "SubmissionDTO" - {

    "must round-trip (serialize/deserialize) a PsaSubmissionDTO" in {
      val dto: SubmissionDTO =
        PsaSubmissionDTO(
          referenceId = TransferNumber("ref-X"),
          userId      = PsaId("A1234567"),
          lastUpdated = ts
        )

      val json   = Json.toJson(dto)
      val parsed = json.validate[SubmissionDTO]

      parsed mustBe JsSuccess(dto)
    }

    "must round-trip (serialize/deserialize) a PspSubmissionDTO" in {
      val dto: SubmissionDTO =
        PspSubmissionDTO(
          referenceId = TransferNumber("ref-Y"),
          userId      = PspId("X9999999"),
          psaId       = PsaId("A7654321"),
          lastUpdated = ts
        )

      val json   = Json.toJson(dto)
      val parsed = json.validate[SubmissionDTO]

      parsed mustBe JsSuccess(dto)
    }

    "must read a PSA payload into PsaSubmissionDTO via discriminator" in {
      val json = Json.obj(
        "referenceId" -> "ignored",
        "userType"    -> "Psa",
        "userId"      -> Json.obj("value" -> "A1234567"),
        "lastUpdated" -> ts
      )

      val parsed = json.validate[SubmissionDTO]
      parsed     mustBe a[JsSuccess[_]]
      parsed.get mustBe PsaSubmissionDTO(TransferNumber("ignored"), Psa, PsaId("A1234567"), ts)
    }

    "must read a PSP payload into PspSubmissionDTO via discriminator" in {
      val json = Json.obj(
        "referenceId" -> "ignored",
        "userType"    -> "Psp",
        "userId"      -> Json.obj("value" -> "X9999999"),
        "psaId"       -> Json.obj("value" -> "A7654321"),
        "lastUpdated" -> ts
      )

      val parsed = json.validate[SubmissionDTO]
      parsed     mustBe a[JsSuccess[_]]
      parsed.get mustBe PspSubmissionDTO(TransferNumber("ignored"), Psp, PspId("X9999999"), PsaId("A7654321"), ts)
    }

    "normalise must map PSA -> PsaSubmitter and psaId = userId, and override referenceId" in {
      val dto =
        PsaSubmissionDTO(
          referenceId = TransferNumber("will-be-overridden"),
          userId      = PsaId("A1234567"),
          lastUpdated = ts
        )

      val normalised = dto.normalise(withReferenceId = TransferNumber("path-ref"), psaUser)
      normalised mustBe NormalisedSubmission(
        referenceId          = TransferNumber("path-ref"),
        userId               = PsaId("A1234567"),
        maybeAssociatedPsaId = None,
        lastUpdated          = ts,
        authenticatedUser    = psaUser
      )
    }

    "normalise must map PSP -> PspSubmitter and keep psaId from payload, and override referenceId" in {
      val dto =
        PspSubmissionDTO(
          referenceId = TransferNumber("will-be-overridden"),
          userId      = PspId("X9999999"),
          psaId       = PsaId("A7654321"),
          lastUpdated = ts
        )

      val normalised = dto.normalise(withReferenceId = TransferNumber("path-ref"), psaUser)
      normalised mustBe NormalisedSubmission(
        referenceId          = TransferNumber("path-ref"),
        userId               = PspId("X9999999"),
        maybeAssociatedPsaId = Some(PsaId("A7654321")),
        lastUpdated          = ts,
        authenticatedUser    = psaUser
      )
    }

    "must fail to deserialize when userType is invalid" in {
      val json = Json.obj(
        "referenceId" -> "ref-Z",
        "userType"    -> "Nope",
        "userId"      -> Json.obj("value" -> "A1234567"),
        "lastUpdated" -> ts
      )

      val result = json.validate[SubmissionDTO]
      result.isError mustBe true
    }

    "must fail to deserialize PSP when required psaId is missing" in {
      val json = Json.obj(
        "referenceId" -> "ref-Z",
        "userType"    -> "Psp",
        "userId"      -> Json.obj("value" -> "X9999999"),
        "lastUpdated" -> ts
      )

      val result = json.validate[SubmissionDTO]
      result.isError mustBe true
    }
  }
}
