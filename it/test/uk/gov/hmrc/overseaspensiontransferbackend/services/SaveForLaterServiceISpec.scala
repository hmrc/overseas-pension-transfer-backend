package uk.gov.hmrc.overseaspensiontransferbackend.services

import play.api.libs.json._
import uk.gov.hmrc.overseaspensiontransferbackend.base.BaseISpec
import uk.gov.hmrc.overseaspensiontransferbackend.models.{AnswersData, SavedUserAnswers}
import uk.gov.hmrc.overseaspensiontransferbackend.repositories.SaveForLaterRepository


class SaveForLaterServiceISpec extends BaseISpec {

  lazy val repository: SaveForLaterRepository = app.injector.instanceOf[SaveForLaterRepository]
  lazy val service: SaveForLaterService       = app.injector.instanceOf[SaveForLaterService]

  lazy private val id  = freshId()
  lazy private val now = frozenNow()


  "SaveForLaterService" should {

    "transform and persist data correctly" in {

      val rawJson = parseJson(
        """
        {
          "memberDetails": {
            "name": { "firstName": "Test", "lastName": "McTest" },
            "nino": "AB123456B",
            "dateOfBirth": "2011-06-05",
            "principalResAddDetails": {
              "addressLine1": "1",
              "addressLine2": "Test road",
              "addressLine3": "Testville",
              "addressLine4": "East Testerly",
              "country": { "code": "AE-AZ", "name": "Abu Dhabi" },
              "ukPostCode": "ab123c",
              "poBoxNumber": "baa"
            },
            "memUkResident": false,
            "memEverUkResident": true,
            "lastPrincipalAddDetails": {
              "addressLine1": "Flat 2",
              "addressLine2": "7 Other Place",
              "addressLine3": "Some District",
              "ukPostCode": "ZZ1 1ZZ"
            },
            "dateMemberLeftUk": "2011-06-06"
          }
        }
        """
      )

      val dto = withSavedDto(id, rawJson, now)

      await(service.saveAnswer(dto)) shouldBe Right(())

      val result = await(repository.get(id)).value
      result.referenceId shouldBe id
      result.lastUpdated shouldBe now

      val memberDetails = Json.toJsObject(result.data) \ "transferringMember" \ "memberDetails"

      assertMemberDetails(memberDetails, Map(
        "foreName"    -> "Test",
        "lastName"    -> "McTest",
        "nino"        -> "AB123456B",
        "dateOfBirth" -> "2011-06-05"
      ))

      val principal = memberDetails \ "principalResAddDetails"
      assertAddress(principal \ "addressDetails", Map(
        "addressLine1" -> "1",
        "addressLine2" -> "Test road",
        "addressLine3" -> "Testville",
        "addressLine4" -> "East Testerly",
        "ukPostCode"   -> "ab123c"
      ))
      assertCountry(principal \ "addressDetails" \ "country", "AE-AZ", "Abu Dhabi")
      (principal \ "poBoxNumber").as[String] shouldBe "baa"

      val residency = memberDetails \ "memberResidencyDetails"
      assertMemberDetails(residency, Map(
        "memUkResident"     -> "No",
        "memEverUkResident" -> "Yes"
      ))

      val lastUK = residency \ "lastPrincipalAddDetails"
      assertAddress(lastUK \ "addressDetails", Map(
        "addressLine1" -> "Flat 2",
        "addressLine2" -> "7 Other Place",
        "addressLine3" -> "Some District",
        "ukPostCode"   -> "ZZ1 1ZZ"
      ))
      (lastUK \ "dateMemberLeftUk").as[String] shouldBe "2011-06-06"
    }

    "retrieve and transform data" in {

      val savedAnswers = SavedUserAnswers(id, parseJson(
        """
        {
          "transferringMember": {
            "memberDetails": {
              "foreName": "GetOnly",
              "lastName": "McLoad",
              "nino": "GG123456G",
              "dateOfBirth": "2012-12-12",
              "principalResAddDetails": {
                "addressDetails": {
                  "addressLine1": "1 Get St",
                  "addressLine2": "Loadtown",
                  "addressLine3": "Getshire",
                  "addressLine4": "East Loadonia",
                  "addressLine5": "Zone 9",
                  "ukPostCode": "LO4 4AD",
                  "country": { "code": "IE", "name": "Ireland" }
                },
                "poBoxNumber": "PO456"
              },
              "memberResidencyDetails": {
                "memUkResident": "No",
                "memEverUkResident": "Yes",
                "lastPrincipalAddDetails": {
                  "addressDetails": {
                    "addressLine1": "Flat 8",
                    "addressLine2": "Memory Lane",
                    "addressLine3": "Recall District",
                    "addressLine4": "Echoes",
                    "ukPostCode": "ZZ2 2ZZ",
                    "country": { "code": "UK", "name": "United Kingdom" }
                  },
                  "dateMemberLeftUk": "2013-05-01"
                }
              }
            }
          }
        }
        """).as[AnswersData], now)

      await(repository.set(savedAnswers)) shouldBe true

      val result = await(service.getAnswers(id))

      result match {
        case Right(dto) =>
          val details = dto.data \ "memberDetails"

          assertMemberDetails(details \ "name", Map(
            "firstName" -> "GetOnly",
            "lastName"  -> "McLoad"
          ))

          assertMemberDetails(details, Map(
            "nino"        -> "GG123456G",
            "dateOfBirth" -> "2012-12-12"
          ))

          val principal = details \ "principalResAddDetails"
          assertAddress(principal, Map(
            "addressLine1" -> "1 Get St",
            "addressLine2" -> "Loadtown",
            "addressLine3" -> "Getshire",
            "addressLine4" -> "East Loadonia",
            "addressLine5" -> "Zone 9",
            "ukPostCode"   -> "LO4 4AD"
          ))
          assertCountry(principal \ "country", "IE", "Ireland")
          (principal \ "poBoxNumber").as[String] shouldBe "PO456"

          (details \ "memUkResident").as[Boolean]     shouldBe false
          (details \ "memEverUkResident").as[Boolean] shouldBe true

          val lastUK = details \ "lastPrincipalAddDetails"
          assertAddress(lastUK, Map(
            "addressLine1" -> "Flat 8",
            "addressLine2" -> "Memory Lane",
            "addressLine3" -> "Recall District",
            "addressLine4" -> "Echoes",
            "ukPostCode"   -> "ZZ2 2ZZ"
          ))
          assertCountry(lastUK \ "country", "UK", "United Kingdom")
          (lastUK \ "dateMemberLeftUk").as[String] shouldBe "2013-05-01"

        case Left(err) =>
          fail(s"Expected successful result but got error: $err")
      }
    }

    "merge and overwrite specific fields without affecting others" in {

      val original = parseJson(
        """
        {
          "memberDetails": {
            "name": { "firstName": "Original", "lastName": "Name" },
            "nino": "AA111111A",
            "dateOfBirth": "1990-01-01",
            "principalResAddDetails": {
              "addressLine1": "10 Main Street",
              "addressLine2": "Townsville",
              "ukPostCode": "AB1 2CD",
              "country": { "code": "UK", "name": "United Kingdom" },
              "poBoxNumber": "PO123"
            },
            "memUkResident": true,
            "memEverUkResident": false,
            "lastPrincipalAddDetails": {
              "addressLine1": "Old Address",
              "addressLine2": "Old Town",
              "ukPostCode": "ZZ9 9ZZ"
            },
            "dateMemberLeftUk": "2010-10-10"
          }
        }
        """
      )

      val update = parseJson(
        """
        {
          "memberDetails": {
            "name": { "firstName": "Updated", "lastName": "User" }
          }
        }
        """
      )

      await(service.saveAnswer(withSavedDto(id, original, now))) shouldBe Right(())
      await(service.saveAnswer(withSavedDto(id, update, now.plusSeconds(1)))) shouldBe Right(())

      val result = await(service.getAnswers(id))

      result match {
        case Right(dto) =>
          val details = dto.data \ "memberDetails"

          assertMemberDetails(details \ "name", Map(
            "firstName" -> "Updated",
            "lastName"  -> "User"
          ))

          assertMemberDetails(details, Map(
            "nino"        -> "AA111111A",
            "dateOfBirth" -> "1990-01-01"
          ))

          val address = details \ "principalResAddDetails"
          assertAddress(address, Map(
            "addressLine1" -> "10 Main Street",
            "addressLine2" -> "Townsville",
            "ukPostCode"   -> "AB1 2CD"
          ))
          assertCountry(address \ "country", "UK", "United Kingdom")
          (address \ "poBoxNumber").as[String] shouldBe "PO123"

          (details \ "memUkResident").as[Boolean]     shouldBe true
          (details \ "memEverUkResident").as[Boolean] shouldBe false

          val lastUK = details \ "lastPrincipalAddDetails"
          assertAddress(lastUK, Map(
            "addressLine1" -> "Old Address",
            "addressLine2" -> "Old Town",
            "ukPostCode"   -> "ZZ9 9ZZ"
          ))
          (lastUK \ "dateMemberLeftUk").as[String] shouldBe "2010-10-10"

        case Left(err) =>
          fail(s"Expected successful result but got error: $err")
      }
    }
  }
}
