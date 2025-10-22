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

import uk.gov.hmrc.overseaspensiontransferbackend.models.transfer.{QtNumber, TransferId, TransferIdentifierInvalid, TransferRetrievalError}
import uk.gov.hmrc.overseaspensiontransferbackend.models._

import scala.util.{Success, Try}

sealed trait GetSpecificTransferHandler {
  def pstr: PstrNumber
  def qtStatus: QtStatus
}

object GetSpecificTransferHandler {

  def apply(
      referenceId: TransferId,
      pstr: PstrNumber,
      qtStatus: QtStatus,
      versionNumber: Option[String]
    ): Either[TransferRetrievalError, GetSpecificTransferHandler] = {
    (qtStatus, versionNumber) match {
      case (InProgress, None)                                      => Right(GetSaveForLaterRecord(referenceId, pstr, qtStatus))
      case (AmendInProgress | Submitted | Compiled, Some(version)) =>
        referenceId match {
          case qtNumber @ QtNumber(_) => Right(GetEtmpRecord(qtNumber, pstr, qtStatus, version))
          case _                      => Left(TransferIdentifierInvalid("[GetSpecificTransferDTO][apply] QtNumber is invalid format"))
        }
      case _                                                       =>
        Left(TransferIdentifierInvalid("[GetSpecificTransferDTO][apply] request parameters invalid for request for transfer data"))
    }
  }
}

final case class GetSaveForLaterRecord(
    referenceId: TransferId,
    pstr: PstrNumber,
    qtStatus: QtStatus
  ) extends GetSpecificTransferHandler

final case class GetEtmpRecord(
    referenceId: QtNumber,
    pstr: PstrNumber,
    qtStatus: QtStatus,
    versionNumber: String
  ) extends GetSpecificTransferHandler
