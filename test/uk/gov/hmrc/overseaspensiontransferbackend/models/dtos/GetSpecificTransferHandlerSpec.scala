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
import uk.gov.hmrc.overseaspensiontransferbackend.models.submission.{QtNumber, TransferIdentifierInvalid}
import uk.gov.hmrc.overseaspensiontransferbackend.models.{Compiled, InProgress, PstrNumber, Submitted}

class GetSpecificTransferHandlerSpec extends AnyFreeSpec with Matchers {

  "apply" - {
    "when qtStatus is InProgress return Right GetSaveForLaterRecord" in {
      GetSpecificTransferHandler.apply("refId", PstrNumber("12345678AB"), InProgress, None) mustBe
        Right(GetSaveForLaterRecord("refId", PstrNumber("12345678AB"), InProgress))
    }

    "when qtStatus is Submitted and versionNumber is defined" in {
      GetSpecificTransferHandler.apply("QT123456", PstrNumber("12345678AB"), Submitted, Some("001")) mustBe
        Right(GetEtmpRecord(QtNumber("QT123456"), PstrNumber("12345678AB"), Submitted, "001"))
    }

    "when qtStatus is Compiled and versionNumber is defined" in {
      GetSpecificTransferHandler.apply("QT123456", PstrNumber("12345678AB"), Compiled, Some("001")) mustBe
        Right(GetEtmpRecord(QtNumber("QT123456"), PstrNumber("12345678AB"), Compiled, "001"))
    }

    "return Left TransferIdentifierInvalid" - {
      "qtStatus is InProgress and versionNumber is definined" in {
        GetSpecificTransferHandler.apply("refId", PstrNumber("12345678AB"), InProgress, Some("001")) mustBe
          Left(TransferIdentifierInvalid("[GetSpecificTransferDTO][apply] request parameters invalid for request for transfer data"))
      }

      "qtStatus is Submitted and versionNumber is None" in {
        GetSpecificTransferHandler.apply("QT123456", PstrNumber("12345678AB"), Submitted, None) mustBe
          Left(TransferIdentifierInvalid("[GetSpecificTransferDTO][apply] request parameters invalid for request for transfer data"))
      }

      "qtNumber is invalid" in {
        GetSpecificTransferHandler.apply("refId", PstrNumber("12345678AB"), Submitted, Some("001")) mustBe
          Left(TransferIdentifierInvalid("[GetSpecificTransferDTO][apply] QtNumber is invalid format"))
      }
    }
  }

}
