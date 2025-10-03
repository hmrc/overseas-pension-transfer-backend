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
  val value: Option[BigDecimal]
  val shareTotal: Option[Int]
  val company: Option[String]
  val shareClass: Option[String]

  def toDownstreamJson: JsValue = Json.toJson[Shares](this)
}

object Shares {

  implicit val writes: Writes[Shares] = (
    (__ \ "valueOfShares").writeNullable[BigDecimal] and
      (__ \ "numberOfShares").writeNullable[Int] and
      (__ \ "companyName").writeNullable[String] and
      (__ \ "classOfShares").writeNullable[String]
  )(shares => (shares.value, shares.shareTotal, shares.company, shares.shareClass))
}

case class UnquotedShares(
    value: Option[BigDecimal],
    shareTotal: Option[Int],
    company: Option[String],
    shareClass: Option[String]
  ) extends Shares

object UnquotedShares {

  implicit val upstreamReads: Reads[UnquotedShares] = (
    (__ \ "valueOfShares").readNullable[BigDecimal] and
      (__ \ "numberOfShares").readNullable[Int] and
      (__ \ "companyName").readNullable[String] and
      (__ \ "classOfShares").readNullable[String]
  )(UnquotedShares.apply _)

  implicit val reads: Reads[UnquotedShares] = (
    (__ \ "unquotedValue").readNullable[BigDecimal] and
      (__ \ "unquotedShareTotal").readNullable[Int] and
      (__ \ "unquotedCompany").readNullable[String] and
      (__ \ "unquotedClass").readNullable[String]
  )(UnquotedShares.apply _)

  implicit val writes: Writes[UnquotedShares] = (
    (__ \ "unquotedValue").writeNullable[BigDecimal] and
      (__ \ "unquotedShareTotal").writeNullable[Int] and
      (__ \ "unquotedCompany").writeNullable[String] and
      (__ \ "unquotedClass").writeNullable[String]
  )(us => (us.value, us.shareTotal, us.company, us.shareClass))

  implicit val format: Format[UnquotedShares] = Format(reads, writes)
}

case class QuotedShares(
    value: Option[BigDecimal],
    shareTotal: Option[Int],
    company: Option[String],
    shareClass: Option[String]
  ) extends Shares

object QuotedShares {

  implicit val upstreamReads: Reads[QuotedShares] = (
    (__ \ "valueOfShares").readNullable[BigDecimal] and
      (__ \ "numberOfShares").readNullable[Int] and
      (__ \ "companyName").readNullable[String] and
      (__ \ "classOfShares").readNullable[String]
  )(QuotedShares.apply _)

  implicit val reads: Reads[QuotedShares] = (
    (__ \ "quotedValue").readNullable[BigDecimal] and
      (__ \ "quotedShareTotal").readNullable[Int] and
      (__ \ "quotedCompany").readNullable[String] and
      (__ \ "quotedClass").readNullable[String]
  )(QuotedShares.apply _)

  implicit val writes: Writes[QuotedShares] = (
    (__ \ "quotedValue").writeNullable[BigDecimal] and
      (__ \ "quotedShareTotal").writeNullable[Int] and
      (__ \ "quotedCompany").writeNullable[String] and
      (__ \ "quotedClass").writeNullable[String]
  )(qs => (qs.value, qs.shareTotal, qs.company, qs.shareClass))

  implicit val format: Format[QuotedShares] = Format(reads, writes)
}
