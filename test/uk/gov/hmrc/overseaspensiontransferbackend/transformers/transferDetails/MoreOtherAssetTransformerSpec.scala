package uk.gov.hmrc.overseaspensiontransferbackend.transformers.transferDetails

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class MoreOtherAssetTransformerSpec extends AnyFreeSpec with Matchers {

  private val transformer: MoreOtherAssetTransformer = new MoreOtherAssetTransformer

  "MoreOtherAssetTransformer" - {
    "must convert transferDetails.moreAsset to transferDetails.typeOfAssets.moreAsset boolean to nested Yes/No string" in {
      val input    = Json.obj("transferDetails" -> Json.obj("moreAsset" -> true))
      val expected = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj("moreAsset" -> "Yes")))

      transformer.construct(input) mustBe Right(expected)
    }

    "must convert nested Yes/No string back to boolean" in {
      val input    = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj("moreAsset" -> "No")))
      val expected = Json.obj("transferDetails" -> Json.obj("moreAsset" -> false))

      transformer.deconstruct(input) mustBe Right(expected)
    }

    "must leave JSON unchanged if moreAsset is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj())

      transformer.construct(input) mustBe Right(input)
    }

    "must leave JSON unchanged if transferDetails.typeOfAssets.moreAsset is missing" in {
      val input = Json.obj("transferDetails" -> Json.obj("typeOfAssets" -> Json.obj()))

      transformer.deconstruct(input) mustBe Right(input)
    }
  }
}
