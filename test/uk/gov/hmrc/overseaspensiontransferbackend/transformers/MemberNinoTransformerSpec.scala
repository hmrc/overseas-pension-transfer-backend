package uk.gov.hmrc.overseaspensiontransferbackend.transformers

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

class MemberNinoTransformerSpec extends AnyWordSpec with Matchers {

  val transformer = new MemberNinoTransformer

  "MemberNinoTransformer" should {

    "construct: move memberDetails.nino to transferringMember.memberDetails.nino" in {
      val inputJson = Json.obj(
        "memberDetails" -> Json.obj(
          "nino" -> "AB123456C"
        )
      )

      val expected = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "nino" -> "AB123456C"
          )
        )
      )

      val result = transformer.construct(inputJson)
      result shouldBe Right(expected)
    }

    "deconstruct: move transferringMember.memberDetails.nino to memberDetails.nino" in {
      val inputJson = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "nino" -> "AB123456C"
          )
        )
      )

      val expected = Json.obj(
        "memberDetails" -> Json.obj(
          "nino" -> "AB123456C"
        )
      )

      val result = transformer.deconstruct(inputJson)
      result shouldBe Right(expected)
    }

    "construct: leave JSON unchanged if memberDetails.nino is missing" in {
      val inputJson = Json.obj("memberDetails" -> Json.obj())

      val result = transformer.construct(inputJson)
      result shouldBe Right(Json.obj("memberDetails" -> Json.obj()))
    }

    "deconstruct: leave JSON unchanged if transferringMember.memberDetails.nino is missing" in {
      val inputJson = Json.obj(
        "transferringMember" -> Json.obj("memberDetails" -> Json.obj())
      )

      val result = transformer.deconstruct(inputJson)
      result shouldBe Right(Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj())))
    }
  }
}
