package uk.gov.hmrc.overseaspensiontransferbackend.transformers

import org.mockito.ArgumentMatchers.any
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import play.api.libs.json._

class TransferringMemberTransformerSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  val incomingJsonMemberDetails: JsObject = Json.obj(
    "memberDetails" -> Json.obj(
      "memberName" -> Json.obj(
        "firstName" -> "Bill",
        "lastName"  -> "Withers"
      )
    )
  )

  val expectedTransformedMemberDetails: JsObject = Json.obj(
    "memberName" -> Json.obj(
      "firstName" -> "B.",
      "lastName"  -> "Withers"
    )
  )

  val incomingJsonMemberResidencyDetails: JsObject = Json.obj(
    "memberDetails" -> Json.obj(
      "memberResidencyDetails" -> Json.obj("memUkResident" -> true)
    )
  )

  val missingMemDetailsJson: JsObject = Json.obj(
    "Hello there" -> Json.obj("General Kenobi" -> true)
  )

  "TransferringMemberTransformer" - {

    "should construct transferringMember json using nested transformers" in {
      val mockTransformer = mock[Transformer]
      when(mockTransformer.construct(any[JsObject])) thenReturn Right(expectedTransformedMemberDetails)

      val transformer = new TransferringMemberTransformer(Seq(mockTransformer))

      val result = transformer.construct(incomingJsonMemberDetails)

      result mustBe Right(
        Json.obj("transferringMember" -> Json.obj("memberDetails" -> expectedTransformedMemberDetails))
      )

      verify(mockTransformer).construct(Json.obj(
        "memberName" -> Json.obj(
          "firstName" -> "Bill",
          "lastName"  -> "Withers"
        )
      ))
    }

    "should deconstruct memberDetails json from transferringMember using nested transformers" in {
      val mockTransformer = mock[Transformer]
      when(mockTransformer.deconstruct(any[JsObject])) thenReturn Right(expectedTransformedMemberDetails)

      val transformer = new TransferringMemberTransformer(Seq(mockTransformer))

      val input = Json.obj("transferringMember" -> Json.obj("memberDetails" -> Json.obj(
        "memberName" -> Json.obj("firstName" -> "B.", "lastName" -> "Withers")
      )))

      val result = transformer.deconstruct(input)

      result mustBe Right(Json.obj("memberDetails" -> expectedTransformedMemberDetails))
      verify(mockTransformer).deconstruct(Json.obj(
        "memberName" -> Json.obj("firstName" -> "B.", "lastName" -> "Withers")
      ))
    }

    "should construct transferringMember json without any transformers" in {
      val transformer = new TransferringMemberTransformer()

      val result = transformer.construct(incomingJsonMemberResidencyDetails)

      result mustBe Right(
        Json.obj("transferringMember" -> incomingJsonMemberResidencyDetails)
      )
    }

    "should deconstruct memberResidencyDetails json without any transformers" in {
      val transformer = new TransferringMemberTransformer()

      val input = Json.obj("transferringMember" -> incomingJsonMemberResidencyDetails)
      val result = transformer.deconstruct(input)

      result mustBe Right(incomingJsonMemberResidencyDetails)
    }

    "should return a left JsError from deconstruct if transferringMember does not exist" in {
      val transformer = new TransferringMemberTransformer()

      transformer.deconstruct(missingMemDetailsJson) mustBe
        Left(JsError("transferringMember.memberDetails not found in input JSON"))
    }

    "should return a left JsError from construct if memberDetails is missing" in {
      val transformer = new TransferringMemberTransformer()

      transformer.construct(missingMemDetailsJson) mustBe
        Left(JsError("memberDetails not found in input JSON"))
    }
  }
}
