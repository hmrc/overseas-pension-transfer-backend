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

package uk.gov.hmrc.overseaspensiontransferbackend.models

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{__, Format, JsValue, Json, Reads, Writes}

trait Shares {
  val value: BigDecimal
  val shareTotal: Int
  val company: String
  val shareClass: String

  def toDownstreamJson: JsValue = Json.toJson[Shares](this)
}

object Shares {

  implicit val writes: Writes[Shares] = (
    (__ \ "valueOfShares").write[BigDecimal] and
      (__ \ "numberOfShares").write[Int] and
      (__ \ "companyName").write[String] and
      (__ \ "classOfShares").write[String]
  )(shares => (shares.value, shares.shareTotal, shares.company, shares.shareClass))
}

case class UnquotedShares(
    value: BigDecimal,
    shareTotal: Int,
    company: String,
    shareClass: String
  ) extends Shares

object UnquotedShares {

  implicit val upstreamReads: Reads[UnquotedShares] = (
    (__ \ "valueOfShares").read[BigDecimal] and
      (__ \ "numberOfShares").read[Int] and
      (__ \ "companyName").read[String] and
      (__ \ "classOfShares").read[String]
  )(UnquotedShares.apply _)

  implicit val reads: Reads[UnquotedShares] = (
    (__ \ "unquotedValue").read[BigDecimal] and
      (__ \ "unquotedShareTotal").read[Int] and
      (__ \ "unquotedCompany").read[String] and
      (__ \ "unquotedClass").read[String]
  )(UnquotedShares.apply _)

  implicit val writes: Writes[UnquotedShares] = (
    (__ \ "unquotedValue").write[BigDecimal] and
      (__ \ "unquotedShareTotal").write[Int] and
      (__ \ "unquotedCompany").write[String] and
      (__ \ "unquotedClass").write[String]
  )(us => (us.value, us.shareTotal, us.company, us.shareClass))

  implicit val format: Format[UnquotedShares] = Format(reads, writes)
}

case class QuotedShares(
    value: BigDecimal,
    shareTotal: Int,
    company: String,
    shareClass: String
  ) extends Shares

object QuotedShares {

  implicit val upstreamReads: Reads[QuotedShares] = (
    (__ \ "valueOfShares").read[BigDecimal] and
      (__ \ "numberOfShares").read[Int] and
      (__ \ "companyName").read[String] and
      (__ \ "classOfShares").read[String]
  )(QuotedShares.apply _)

  implicit val reads: Reads[QuotedShares] = (
    (__ \ "quotedValue").read[BigDecimal] and
      (__ \ "quotedShareTotal").read[Int] and
      (__ \ "quotedCompany").read[String] and
      (__ \ "quotedClass").read[String]
  )(QuotedShares.apply _)

  implicit val writes: Writes[QuotedShares] = (
    (__ \ "quotedValue").write[BigDecimal] and
      (__ \ "quotedShareTotal").write[Int] and
      (__ \ "quotedCompany").write[String] and
      (__ \ "quotedClass").write[String]
  )(qs => (qs.value, qs.shareTotal, qs.company, qs.shareClass))

  implicit val format: Format[QuotedShares] = Format(reads, writes)
}
