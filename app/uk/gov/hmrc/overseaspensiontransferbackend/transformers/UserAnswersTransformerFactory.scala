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

package uk.gov.hmrc.overseaspensiontransferbackend.transformers

import uk.gov.hmrc.overseaspensiontransferbackend.transformers.aboutReceivingQROPS._
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferDetails.{
  ApplicableExclusionTransformer,
  CashOnlyTransferTransformer,
  PaymentTaxableOverseasTransformer
}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferringMember._

case class UserAnswersTransformerFactory() {

  private def memberDetailsTransformers: Seq[Transformer] = Seq(
    new MemberNameTransformer(),
    new MemberDOBTransformer(),
    new MemberNinoTransformer(),
    new MemberNoNinoTransformer(),
    new MemberAddressTransformer(),
    new MemberIsUKResidentTransformer(),
    new MemberEverUKResidentTransformer(),
    new MemberLastUKAddressTransformer(),
    new MemberDateLeftUKTransformer()
  )

  private def qropsDetailsTransformers: Seq[Transformer] = Seq(
    new QropsNameTransformer(),
    new QropsRefTransformer(),
    new QropsAddressTransformer(),
    new QropsEstablishedCountryTransformer(),
    new QropsEstablishedOtherTransformer()
  )

  private def qropsSchemeManagerDetailsTransformers: Seq[Transformer] = Seq(
    new QropsSchemeManagerTypeTransformer,
    new QropsSchemeManagerAddressTransformer,
    new QropsSchemeManagerEmailTransformer,
    new QropsSchemeManagerPhoneTranformer,
    new QropsSchemeManagerIndividualTransformer
  )

  private def transferDetailsTransformers: Seq[Transformer] = Seq(
    new CashOnlyTransferTransformer,
    new PaymentTaxableOverseasTransformer,
    new ApplicableExclusionTransformer
  )

  def build(): UserAnswersTransformer = {
    val allTransformers =
      memberDetailsTransformers ++ qropsDetailsTransformers ++ qropsSchemeManagerDetailsTransformers ++ transferDetailsTransformers

    new UserAnswersTransformer(allTransformers)
  }
}
