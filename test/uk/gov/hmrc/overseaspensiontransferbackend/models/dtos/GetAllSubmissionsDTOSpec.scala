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
import uk.gov.hmrc.overseaspensiontransferbackend.models.PstrNumber
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.AllTransfersItem

import java.time.{Clock, Instant, ZoneOffset}

class GetAllTransfersDTOSpec extends AnyFreeSpec with Matchers {

  private val fixedInstant = Instant.parse("2025-09-16T12:00:00Z")
  private val fixedClock   = Clock.fixed(fixedInstant, ZoneOffset.UTC)

  private val pstrAsSent = "12 34 56 78 AB"
  private val pstr       = PstrNumber(pstrAsSent)

  private val itemForPstr =
    AllTransfersItem(
      transferReference = None,
      qtReference       = None,
      qtVersion         = None,
      nino              = None,
      memberFirstName   = None,
      memberSurname     = None,
      submissionDate    = None,
      qtStatus          = None,
      pstrNumber        = Some(pstr)
    )

  "GetAllTransfersDTO.from" - {
    "must set pstr, transfers, and lastUpdated from the provided Clock" in {
      val dto = GetAllTransfersDTO.from(pstr, Seq(itemForPstr))(fixedClock)

      dto.pstr        mustBe pstr
      dto.transfers   mustBe Seq(itemForPstr)
      dto.lastUpdated mustBe fixedInstant
    }

    "must throw when transfers is empty" in {
      val ex = the[IllegalArgumentException] thrownBy {
        GetAllTransfersDTO.from(pstr, Nil)(fixedClock)
      }
      ex.getMessage must include("transfers must be non-empty")
    }
  }

  "GetAllTransfersDTO.format" - {
    "must round-trip (serialize/deserialize) preserving values" in {
      val dto    = GetAllTransfersDTO.from(pstr, Seq(itemForPstr))(fixedClock)
      val json   = Json.toJson(dto)
      val parsed = json.validate[GetAllTransfersDTO]

      parsed mustBe JsSuccess(dto)
    }

    "must write pstr as a JSON string exactly as stored (no normalisation)" in {
      val dto  = GetAllTransfersDTO.from(pstr, Seq(itemForPstr))(fixedClock)
      val json = Json.toJson(dto)

      (json \ "pstr") mustBe JsDefined(JsString(pstrAsSent))
    }

    "must include transfers as an array" in {
      val dto  = GetAllTransfersDTO.from(pstr, Seq(itemForPstr))(fixedClock)
      val json = Json.toJson(dto)

      (json \ "transfers").as[JsArray].value.size mustBe 1
    }
  }
}
