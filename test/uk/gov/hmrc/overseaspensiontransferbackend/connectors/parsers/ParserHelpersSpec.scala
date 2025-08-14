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

package uk.gov.hmrc.overseaspensiontransferbackend.connectors.parsers

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import play.api.http.Status._
import uk.gov.hmrc.overseaspensiontransferbackend.connectors.parsers.ParserHelpers.MaxSnippet
import uk.gov.hmrc.overseaspensiontransferbackend.models.downstream._

class ParserHelpersSpec extends AnyFreeSpec with Matchers {

  "handleDownstreamResponse" - {

    "maps 201 Created to DownstreamSuccess" in {
      val json = Json.parse(
        """
          |{
          |  "success": {
          |    "processingDate": "2025-07-01T10:00:00Z",
          |    "formBundleNumber": "119000004320",
          |    "qtReference": "QT123456"
          |  }
          |}
          |""".stripMargin
      )

      val expected = HttpResponse(CREATED, json, Map.empty)
      val actual   = ParserHelpers.handleDownstreamResponse(expected)

      actual                             mustBe a[Right[_, _]]
      actual.toOption.get.qtNumber.value mustBe "QT123456"
    }

    "maps 400 to HipBadRequest when responseSystemErrorType" in {
      val message =
        "Invalid&#x20;JSON&#x20;message&#x20;content&#x20;used&#x3b;&#x20;Message&#x3a;&#x20;&quot;Expected&#x20;a&#x20;&#x27;,&#x27;&#x20;or&#x20;&#x27;&#x7d;&#x27;&#x20;at&#x20;character&#x20;93&#x20;of&#x20;&#x7b;&#xa;&#x9;&quot;idType&quot;&#x3a;&#x20;&quot;EMPREF&quot;,&#xa;&#x9;&quot;idValue&quot;&#x3a;&#x20;&quot;864FZ00049&quot;,&#xa;&#x9;&quot;regimeType&quot;&#x3a;&#x20;&quot;PAYE&quot;,&#xa;&#x9;&quot;lockReason&quot;&#x3a;&#x20;&quot;8&quot;&#xa;&#x9;&quot;chargeReferences&quot;&#x3a;&#x20;&#x5b;&#xa;&#x9;&#x9;&quot;XM123456789812&quot;,&#xa;&#x9;&#x9;&quot;XM123456781234&quot;,&#xa;&#x9;&#x9;&quot;XC123111781234&quot;&#xa;&#x9;&#x5d;,&#x9;&#xa;&#x9;&quot;createNote&quot;&#x3a;&#x20;true,&#xa;&#x9;&quot;noteType&quot;&#x3a;&#x20;&quot;TTP&quot;,&#xa;&#x9;&quot;noteLines&quot;&#x3a;&#x20;&#x5b;&#xa;&#x9;&#x9;&quot;paymentPlanType&#x3a;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;TTP&quot;,&#xa;&#x9;&#x9;&quot;arrangementAgreedDate&#x3a;&#x20;2022-02-01&quot;,&#xa;&#x9;&#x9;&quot;arrangementChannel&#x3a;&#x20;&#x20;&#x20;&#x20;ePAYE&quot;,&#xa;&#x9;&#x9;&quot;firstPaymentAmount&#x3a;&#x20;&#x20;&#x20;&#x20;1234.56&quot;,&#xa;&#x9;&#x9;&quot;firstPaymentDate&#x3a;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;2022-03-01&quot;,&#xa;&#x9;&#x9;&quot;regularPaymentAmount&#x3a;&#x20;&#x20;1000&quot;,&#xa;&#x9;&#x9;&quot;paymentFrequency&#x3a;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;Monthly&quot;,&#xa;&#x9;&#x9;&quot;arrangementReviewDate&#x3a;&#x20;2022-11-02&quot;,&#xa;&#x9;&#x9;&quot;ddiReference&#x3a;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;&#x20;123456789012&quot;&#xa;&#x9;&#x5d;&#xa;&#x7d;&quot;"

      val json     = Json.parse(
        s"""
           |{
           |  "origin": "HoD",
           |  "response": {
           |    "error": {
           |      "code": "400",
           |      "logID": "C0000AB8190C86300000000200006836",
           |      "message": "$message"
           |    }
           |  }
           |}
           |""".stripMargin.replace("\"A\"*32", "\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"")
      )
      val expected = HttpResponse(BAD_REQUEST, json, Map.empty)
      val actual   = ParserHelpers.handleDownstreamResponse(expected)

      actual                                                       mustBe a[Left[_, _]]
      actual.left.toOption.get                                     mustBe a[HipBadRequest]
      actual.left.toOption.get.asInstanceOf[HipBadRequest].message mustBe message.take(MaxSnippet)
    }

    "maps 400 to HipOriginFailures when failures array present" in {
      val json     = Json.parse(
        """
          |{
          |  "origin": "HIP",
          |  "response": {
          |    "failures": [ { "type":"X", "reason":"Y" } ]
          |  }
          |}
          |""".stripMargin
      )
      val expected = HttpResponse(BAD_REQUEST, json, Map.empty)
      val actual   = ParserHelpers.handleDownstreamResponse(expected)

      actual.left.toOption.get mustBe a[HipOriginFailures]
    }

    "maps 422 to EtmpValidationError" in {
      val json     = Json.parse(
        """
          |{
          |  "errors": {
          |    "processingDate": "2025-07-01T10:00:00Z",
          |    "code": "001",
          |    "text": "REGIME missing or invalid"
          |  }
          |}
          |""".stripMargin
      )
      val expected = HttpResponse(UNPROCESSABLE_ENTITY, json, Map.empty)
      val actual   = ParserHelpers.handleDownstreamResponse(expected)

      actual.left.toOption.get mustBe a[EtmpValidationError]
    }

    "maps 415 to UnsupportedMedia" in {
      val expected = HttpResponse(UNSUPPORTED_MEDIA_TYPE, "")
      ParserHelpers.handleDownstreamResponse(expected).left.toOption.get mustBe UnsupportedMedia
    }

    "maps unknown status to Unexpected" in {
      val expected = HttpResponse(IM_A_TEAPOT, "I'm a teapot")
      ParserHelpers.handleDownstreamResponse(expected).left.toOption.get mustBe Unexpected(IM_A_TEAPOT, "I'm a teapot")
    }
  }
}
