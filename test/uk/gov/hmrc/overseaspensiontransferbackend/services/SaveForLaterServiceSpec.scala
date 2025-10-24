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

package uk.gov.hmrc.overseaspensiontransferbackend.services

import org.apache.pekko.Done
import org.mockito.ArgumentCaptor
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import play.api.libs.json.{JsArray, JsError, JsObject, Json}
import uk.gov.hmrc.overseaspensiontransferbackend.base.SpecBase
import uk.gov.hmrc.overseaspensiontransferbackend.models.dtos.UserAnswersDTO
import uk.gov.hmrc.overseaspensiontransferbackend.models.{AnswersData, SavedUserAnswers, TransferringMember}
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository
import uk.gov.hmrc.overseaspensiontransferbackend.services.SaveForLaterError.DeleteFailed
import uk.gov.hmrc.overseaspensiontransferbackend.transformers.UserAnswersTransformer

import scala.concurrent.Future

class SaveForLaterServiceSpec extends AnyFreeSpec with SpecBase with BeforeAndAfterEach {

  private val mockRepository  = mock[SaveForLaterRepository]
  private val mockTransformer = mock[UserAnswersTransformer]
  private val service         = new SaveForLaterServiceImpl(mockRepository, mockTransformer)

  private val validData: JsObject = Json.obj(
    "memberDetails" -> Json.obj(
      "memberName" -> Json.obj(
        "firstName" -> "Foo",
        "lastName"  -> "Bar"
      )
    )
  )

  private val validSaved = SavedUserAnswers(
    transferId  = testId,
    pstr        = pstr,
    data        = AnswersData(None, Some(TransferringMember(None)), None, None),
    lastUpdated = now
  )

  private val validDTO = UserAnswersDTO(testId, pstr, validData, now)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRepository, mockTransformer)
  }

  private def captureSaved(): SavedUserAnswers = {
    val captor: ArgumentCaptor[SavedUserAnswers] = ArgumentCaptor.forClass(classOf[SavedUserAnswers])
    verify(mockRepository).set(captor.capture())
    captor.getValue
  }

  "SaveForLaterServiceSpec" - {

    "must return Right(UserAnswersDTO) when data exists and deconstruct succeeds" in {
      when(mockRepository.get(testId.value)).thenReturn(Future.successful(Some(validSaved)))
      when(mockTransformer.deconstruct(*)).thenReturn(Right(validData))

      val result = service.getAnswers(testId)

      result.futureValue mustBe Right(validDTO)
    }

    "must return Left(NotFound) when no data exists" in {
      when(mockRepository.get(testId.value)).thenReturn(Future.successful(None))

      val result = service.getAnswers(testId)

      result.futureValue mustBe Left(SaveForLaterError.NotFound)
    }

    "must return Left(TransformationError) when deconstruct fails" in {
      when(mockRepository.get(testId.value)).thenReturn(Future.successful(Some(validSaved)))
      when(mockTransformer.deconstruct(*)).thenReturn(Left(JsError("deconstruct failed")))

      val result = service.getAnswers(testId)

      result.futureValue match {
        case Left(SaveForLaterError.TransformationError(msg)) => msg must include("deconstruct failed")
        case other                                            => fail(s"Unexpected result: $other")
      }
    }

    "must save new answers if valid" in {
      when(mockRepository.get(testId.value)).thenReturn(Future.successful(None))
      when(mockTransformer.construct(*)).thenReturn(Right(validData))
      when(mockRepository.set(*)).thenReturn(Future.successful(true))

      val result = service.saveAnswer(validDTO)

      result.futureValue mustBe Right(())
    }

    "must return Left(SaveFailed) if repo.set returns false" in {
      when(mockRepository.get(testId.value)).thenReturn(Future.successful(None))
      when(mockTransformer.construct(*)).thenReturn(Right(validData))
      when(mockRepository.set(*)).thenReturn(Future.successful(false))

      val result = service.saveAnswer(validDTO)

      result.futureValue mustBe Left(SaveForLaterError.SaveFailed)
    }

    "must merge with existing data before save" in {
      when(mockRepository.get(testId.value)).thenReturn(Future.successful(Some(validSaved)))
      when(mockTransformer.construct(*)).thenReturn(Right(validData))
      when(mockRepository.set(*)).thenReturn(Future.successful(true))

      val result = service.saveAnswer(validDTO)

      result.futureValue mustBe Right(())
    }

    "must return Left(TransformationError) when construct fails" in {
      when(mockTransformer.construct(*)).thenReturn(Left(JsError("construct failed")))

      val result = service.saveAnswer(validDTO)

      result.futureValue match {
        case Left(SaveForLaterError.TransformationError(msg)) => msg must include("construct failed")
        case other                                            => fail(s"Unexpected result: $other")
      }
    }

    "must return Left(TransformationError) when a known field is malformed" in {
      val malformedJson = Json.obj(
        "transferringMember" -> Json.obj(
          "memberDetails" -> Json.obj(
            "nino" -> 12345
          )
        )
      )

      when(mockTransformer.construct(*)).thenReturn(Right(malformedJson))
      when(mockRepository.get(testId.value)).thenReturn(Future.successful(Some(validSaved)))
      when(mockRepository.set(*)).thenReturn(Future.successful(true))

      val result = service.saveAnswer(validDTO)

      result.futureValue match {
        case Left(SaveForLaterError.TransformationError(msg)) => msg.toLowerCase must include("nino")
        case other                                            => fail(s"Expected TransformationError due to malformed field, got: $other")
      }
    }

    "deleteAnswers" - {
      "Return a Right(Done) when repository returns true" in {
        when(mockRepository.clear(*)).thenReturn(Future.successful(true))

        val result = service.deleteAnswers(testId)

        result.futureValue mustBe Right(Done)
      }

      "Return a Left(DeleteFailed) when repository returns false" in {
        when(mockRepository.clear(*)).thenReturn(Future.successful(false))

        val result = service.deleteAnswers(testId)

        result.futureValue mustBe Left(DeleteFailed)
      }
    }

    "must remove memEverUkResident and lastPrincipalAddDetails when memUkResident flips to true, keeping other fields" in {
      val existingJson =
        Json.obj(
          "transferringMember" -> Json.obj(
            "memberDetails" -> Json.obj(
              "foreName"               -> "Ada",
              "lastName"               -> "Lovelace",
              "memberResidencyDetails" -> Json.obj(
                "memUkResident"           -> "false",
                "memEverUkResident"       -> "true",
                "lastPrincipalAddDetails" -> Json.obj(
                  "addressLine1" -> "1 Old Street",
                  "postcode"     -> "AB1 2CD"
                )
              )
            )
          ),
          "reportDetails"      -> Json.obj(
            "pstr"            -> "12345678AA",
            "qtReference"     -> "QTR-0001",
            "qtDigitalStatus" -> "InProgress"
          )
        )

      val updateTransformed =
        Json.obj(
          "transferringMember" -> Json.obj(
            "memberDetails" -> Json.obj(
              "foreName"               -> "Ada",
              "lastName"               -> "Lovelace",
              "memberResidencyDetails" -> Json.obj(
                "memUkResident" -> "true"
              )
            )
          ),
          "reportDetails"      -> Json.obj(
            "pstr"            -> "12345678AA",
            "qtReference"     -> "QTR-0001",
            "qtDigitalStatus" -> "InProgress"
          )
        )

      val existingSaved = validSaved.copy(data = existingJson.as[AnswersData])

      when(mockRepository.get(testId)).thenReturn(Future.successful(Some(existingSaved)))
      when(mockTransformer.construct(*[JsObject])).thenReturn(Right(updateTransformed))
      when(mockRepository.set(*[SavedUserAnswers])).thenReturn(Future.successful(true))

      service.saveAnswer(validDTO).futureValue mustBe Right(())

      val saved = captureSaved()
      val json  = Json.toJsObject(saved.data)

      (json \ "transferringMember" \ "memberDetails" \ "foreName").as[String]                                         mustBe "Ada"
      (json \ "transferringMember" \ "memberDetails" \ "lastName").as[String]                                         mustBe "Lovelace"
      (json \ "transferringMember" \ "memberDetails" \ "memberResidencyDetails" \ "memUkResident").as[String]         mustBe "true"
      (json \ "transferringMember" \ "memberDetails" \ "memberResidencyDetails" \ "memEverUkResident").toOption       mustBe None
      (json \ "transferringMember" \ "memberDetails" \ "memberResidencyDetails" \ "lastPrincipalAddDetails").toOption mustBe None

      (json \ "reportDetails" \ "pstr").as[String]            mustBe "12345678AA"
      (json \ "reportDetails" \ "qtReference").as[String]     mustBe "QTR-0001"
      (json \ "reportDetails" \ "qtDigitalStatus").as[String] mustBe "InProgress"
    }

  }

  "merge & prune – TypeOfAssets (quoted/unquoted shares)" - {

    "must append (replace with updated full array) when a second quoted share is added" in {
      val existingJson =
        Json.obj(
          "transferDetails" -> Json.obj(
            "typeOfAssets"   -> Json.obj(
              "quotedShareAssets" -> "Yes",
              "quotedShares"      -> Json.arr(quotedShare(100, 10, "Alpha PLC", "A"))
            ),
            "transferAmount" -> BigDecimal(9999)
          )
        )

      val updateTransformed =
        Json.obj(
          "transferDetails" -> Json.obj(
            "typeOfAssets"   -> Json.obj(
              "quotedShareAssets" -> "Yes",
              "quotedShares"      -> Json.arr(
                quotedShare(100, 10, "Alpha PLC", "A"),
                quotedShare(250, 25, "Beta PLC", "B")
              )
            ),
            "transferAmount" -> BigDecimal(9999)
          )
        )

      val existingSaved = validSaved.copy(data = existingJson.as[AnswersData])

      when(mockRepository.get(testId)).thenReturn(Future.successful(Some(existingSaved)))
      when(mockTransformer.construct(*[JsObject])).thenReturn(Right(updateTransformed))
      when(mockRepository.set(*[SavedUserAnswers])).thenReturn(Future.successful(true))

      service.saveAnswer(validDTO).futureValue mustBe Right(())

      val saved = captureSaved()
      val json  = Json.toJsObject(saved.data)

      (json \ "transferDetails" \ "transferAmount").as[BigDecimal] mustBe BigDecimal(9999)

      val arr = (json \ "transferDetails" \ "typeOfAssets" \ "quotedShares").as[JsArray]
      arr.value                                       must have size 2
      (arr.value.head \ "quotedCompany").as[String] mustBe "Alpha PLC"
      (arr.value(1) \ "quotedCompany").as[String]   mustBe "Beta PLC"
    }

    "must remove a deleted quoted share when the updated array has fewer elements" in {
      val existingJson =
        Json.obj(
          "transferDetails" -> Json.obj(
            "typeOfAssets" -> Json.obj(
              "quotedShareAssets" -> "Yes",
              "quotedShares"      -> Json.arr(
                quotedShare(100, 10, "Alpha PLC", "A"),
                quotedShare(250, 25, "Beta PLC", "B")
              )
            )
          )
        )

      val updateTransformed =
        Json.obj(
          "transferDetails" -> Json.obj(
            "typeOfAssets" -> Json.obj(
              "quotedShareAssets" -> "Yes",
              "quotedShares"      -> Json.arr(quotedShare(250, 25, "Beta PLC", "B"))
            )
          )
        )

      val existingSaved = validSaved.copy(data = existingJson.as[AnswersData])

      when(mockRepository.get(testId)).thenReturn(Future.successful(Some(existingSaved)))
      when(mockTransformer.construct(*[JsObject])).thenReturn(Right(updateTransformed))
      when(mockRepository.set(*[SavedUserAnswers])).thenReturn(Future.successful(true))

      service.saveAnswer(validDTO).futureValue mustBe Right(())

      val saved = captureSaved()
      val json  = Json.toJsObject(saved.data)

      val arr = (json \ "transferDetails" \ "typeOfAssets" \ "quotedShares").as[JsArray]
      arr.value                                                                          must have size 1
      (arr.value.head \ "quotedCompany").as[String]                                    mustBe "Beta PLC"
      arr.value.exists(v => (v \ "quotedCompany").asOpt[String].contains("Alpha PLC")) mustBe false
    }

    "must remove the entire quotedShares block quotedShareAssets to when omitted in update " in {
      val existingJson =
        Json.obj(
          "transferDetails" -> Json.obj(
            "typeOfAssets" -> Json.obj(
              "quotedShareAssets"   -> "Yes",
              "quotedShares"        -> Json.arr(quotedShare(100, 10, "Alpha PLC", "A")),
              "unquotedShareAssets" -> "Yes",
              "unquotedShares"      -> Json.arr(unquotedShare(300, 30, "Gamma Ltd", "G"))
            )
          )
        )

      val updateTransformed =
        Json.obj(
          "transferDetails" -> Json.obj(
            "typeOfAssets" -> Json.obj(
              "quotedShareAssets"   -> "No",
              "unquotedShareAssets" -> "Yes",
              "unquotedShares"      -> Json.arr(unquotedShare(300, 30, "Gamma Ltd", "G"))
            )
          )
        )

      val existingSaved = validSaved.copy(data = existingJson.as[AnswersData])

      when(mockRepository.get(testId)).thenReturn(Future.successful(Some(existingSaved)))
      when(mockTransformer.construct(*[JsObject])).thenReturn(Right(updateTransformed))
      when(mockRepository.set(*[SavedUserAnswers])).thenReturn(Future.successful(true))

      service.saveAnswer(validDTO).futureValue mustBe Right(())

      val saved = captureSaved()
      val json  = Json.toJsObject(saved.data)

      (json \ "transferDetails" \ "typeOfAssets" \ "quotedShares").toOption        mustBe None
      (json \ "transferDetails" \ "typeOfAssets" \ "quotedShareAssets").as[String] mustBe "No"

      val uArr = (json \ "transferDetails" \ "typeOfAssets" \ "unquotedShares").as[JsArray]
      uArr.value                                         must have size 1
      (uArr.value.head \ "unquotedCompany").as[String] mustBe "Gamma Ltd"
    }

    "transformer must derive 'No' flag from missing empty array" in {
      val existingJson =
        Json.obj(
          "transferDetails" -> Json.obj(
            "typeOfAssets" -> Json.obj(
              "quotedShareAssets"   -> "Yes",
              "quotedShares"        -> Json.arr(
                Json.obj("quotedValue" -> 100, "quotedShareTotal" -> 10, "quotedCompany" -> "Alpha", "quotedClass" -> "A")
              ),
              "unquotedShareAssets" -> "Yes",
              "unquotedShares"      -> Json.arr(
                Json.obj("unquotedValue" -> 300, "unquotedShareTotal" -> 30, "unquotedCompany" -> "Gamma", "unquotedClass" -> "G")
              )
            )
          )
        )

      val dto = validDTO.copy(data =
        Json.obj(
          "transferDetails" -> Json.obj(
            "typeOfAssets" -> Json.obj(
              "unquotedShares" -> Json.arr(
                Json.obj("unquotedValue" -> 300, "unquotedShareTotal" -> 30, "unquotedCompany" -> "Gamma", "unquotedClass" -> "G")
              )
            )
          )
        )
      )

      val updateTransformed =
        Json.obj(
          "transferDetails" -> Json.obj(
            "typeOfAssets" -> Json.obj(
              "quotedShareAssets"   -> "No",
              "unquotedShareAssets" -> "Yes",
              "unquotedShares"      -> Json.arr(
                Json.obj("unquotedValue" -> 300, "unquotedShareTotal" -> 30, "unquotedCompany" -> "Gamma", "unquotedClass" -> "G")
              )
            )
          )
        )

      val existingSaved = validSaved.copy(data = existingJson.as[AnswersData])

      when(mockRepository.get(testId)).thenReturn(Future.successful(Some(existingSaved)))
      when(mockTransformer.construct(*[JsObject])).thenReturn(Right(updateTransformed))
      when(mockRepository.set(*[SavedUserAnswers])).thenReturn(Future.successful(true))

      service.saveAnswer(dto).futureValue mustBe Right(())

      val saved = captureSaved()
      val json  = Json.toJsObject(saved.data)

      (json \ "transferDetails" \ "typeOfAssets" \ "quotedShareAssets").as[String] mustBe "No"
      (json \ "transferDetails" \ "typeOfAssets" \ "quotedShares").toOption        mustBe None

      (json \ "transferDetails" \ "typeOfAssets" \ "unquotedShareAssets").as[String] mustBe "Yes"
      val u = (json \ "transferDetails" \ "typeOfAssets" \ "unquotedShares").as[JsArray]
      u.value                                         must have size 1
      (u.value.head \ "unquotedCompany").as[String] mustBe "Gamma"
    }

    "must preserve unrelated transferDetails fields when only typeOfAssets is touched" in {
      val existingJson =
        Json.obj(
          "transferDetails" -> Json.obj(
            "transferAmount" -> BigDecimal(9999),
            "typeOfAssets"   -> Json.obj(
              "quotedShareAssets" -> "Yes",
              "quotedShares"      -> Json.arr(quotedShare(100, 10, "Alpha PLC", "A"))
            )
          )
        )

      val updateTransformed =
        Json.obj(
          "transferDetails" -> Json.obj(
            "transferAmount" -> BigDecimal(9999),
            "typeOfAssets"   -> Json.obj(
              "quotedShareAssets" -> "Yes",
              "quotedShares"      -> Json.arr(quotedShare(200, 20, "Beta PLC", "B"))
            )
          )
        )

      val existingSaved = validSaved.copy(data = existingJson.as[AnswersData])

      when(mockRepository.get(testId)).thenReturn(Future.successful(Some(existingSaved)))
      when(mockTransformer.construct(*[JsObject])).thenReturn(Right(updateTransformed))
      when(mockRepository.set(*[SavedUserAnswers])).thenReturn(Future.successful(true))

      service.saveAnswer(validDTO).futureValue mustBe Right(())

      val saved = captureSaved()
      val json  = Json.toJsObject(saved.data)

      (json \ "transferDetails" \ "transferAmount").as[BigDecimal] mustBe BigDecimal(9999)

      val arr = (json \ "transferDetails" \ "typeOfAssets" \ "quotedShares").as[JsArray]
      arr.value                                       must have size 1
      (arr.value.head \ "quotedCompany").as[String] mustBe "Beta PLC"
    }
  }

  "deleteAnswers" - {
    "Return a Right(Done) when repository returns true" in {
      when(mockRepository.clear(*[String])).thenReturn(Future.successful(true))
      service.deleteAnswers(testId).futureValue mustBe Right(Done)
    }

    "Return a Left(DeleteFailed) when repository returns false" in {
      when(mockRepository.clear(*[String])).thenReturn(Future.successful(false))
      service.deleteAnswers(testId).futureValue mustBe Left(DeleteFailed)
    }
  }
}
