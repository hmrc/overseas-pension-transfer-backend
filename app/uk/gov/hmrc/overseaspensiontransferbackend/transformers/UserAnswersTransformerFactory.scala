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

import com.google.inject.Inject
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.aboutReceivingQROPS._
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferDetails.{
  AmountTaxDeductedTransformer,
  ApplicableExclusionTransformer,
  AssetTypeTransformer,
  CashAssetsTransformer,
  CashOnlyTransferTransformer,
  CashValueTransformer,
  MoreOtherAssetTransformer,
  MorePropertyTransformer,
  MoreQuotedTransformer,
  MoreUnquotedTransformer,
  OtherAssetsTransformer,
  PaymentTaxableOverseasTransformer,
  PropertyTransformer,
  QuotedSharesTransformer,
  ReasonNoOverseasTransferTransformer,
  TransferMinusTaxTransformer,
  UnquotedSharesTransformer,
  WhyTaxableTransformer
}
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferringMember._
import uk.gov.hmrc.overseaspensiontransferbackend.utils.CountryCodeReader

class UserAnswersTransformerFactory @Inject() (countryCodeReader: CountryCodeReader) {

  private def memberDetailsTransformers: Seq[Transformer] = Seq(
    new MemberNameTransformer(),
    new MemberDOBTransformer(),
    new MemberNinoTransformer(),
    new MemberNoNinoTransformer(),
    new MemberAddressTransformer(countryCodeReader),
    new MemberIsUKResidentTransformer(),
    new MemberEverUKResidentTransformer(),
    new MemberLastUKAddressTransformer(countryCodeReader),
    new MemberDateLeftUKTransformer()
  )

  private def qropsDetailsTransformers: Seq[Transformer] = Seq(
    new QropsNameTransformer(),
    new QropsRefTransformer(),
    new QropsAddressTransformer(countryCodeReader),
    new QropsEstablishedCountryTransformer(countryCodeReader),
    new QropsEstablishedOtherTransformer(countryCodeReader)
  )

  private def qropsSchemeManagerDetailsTransformers: Seq[Transformer] = Seq(
    new QropsSchemeManagerTypeTransformer,
    new QropsSchemeManagerOrganisationNameTransformer,
    new QropsSchemeManagerAddressTransformer(countryCodeReader),
    new QropsSchemeManagerEmailTransformer,
    new QropsSchemeManagerPhoneTransformer,
    new QropsSchemeManagerIndividualTransformer,
    new QropsSchemeManagerOrganisationContactNameTransformer
  )

  private def transferDetailsTransformers: Seq[Transformer] = Seq(
    new CashOnlyTransferTransformer,
    new PaymentTaxableOverseasTransformer,
    new ReasonNoOverseasTransferTransformer,
    new WhyTaxableTransformer,
    new ApplicableExclusionTransformer,
    new AmountTaxDeductedTransformer,
    new TransferMinusTaxTransformer,
    new CashAssetsTransformer,
    new AssetTypeTransformer,
    new CashValueTransformer,
    new MoreQuotedTransformer,
    new MoreUnquotedTransformer,
    new MorePropertyTransformer,
    new MoreOtherAssetTransformer,
    new QuotedSharesTransformer,
    new UnquotedSharesTransformer,
    new PropertyTransformer(countryCodeReader),
    new OtherAssetsTransformer
  )

  def build(): UserAnswersTransformer = {
    val allTransformers =
      memberDetailsTransformers ++ qropsDetailsTransformers ++ qropsSchemeManagerDetailsTransformers ++ transferDetailsTransformers

    new UserAnswersTransformer(allTransformers)
  }
}
