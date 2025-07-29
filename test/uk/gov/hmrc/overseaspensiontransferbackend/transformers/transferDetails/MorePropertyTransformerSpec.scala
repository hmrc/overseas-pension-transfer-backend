package uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferDetails

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class MorePropertyTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer: MorePropertyTransformer = new MorePropertyTransformer

  "MorePropertyTransformer" - {
    "must convert transferDetails.moreProperty to transferDetails.typeOfAssets.moreProperty boolean to nested Yes/No string" in {
      val input    = Json.obj("transferDetails" -> Json.obj("moreProperty" -> true))
      val expected = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj("moreProperty" -> "Yes")))

      transformer.construct(input) mustBe Right(expected)
    }

    "must convert nested Yes/No string back to boolean" in {
      val input    = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj("moreProperty" -> "No")))
      val expected = Json.obj("transferDetails" -> Json.obj("moreProperty" -> false))

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "must leave JSON unchanged if moreProperty is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj())

      transformer.construct(input) mustBe Right(input)
    }

    "must leave JSON unchanged if transferDetails.typeOfAssets.moreProperty is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj()))

      transformer.deconstruct(input) mustBe Right(input)
    }
  }
}
