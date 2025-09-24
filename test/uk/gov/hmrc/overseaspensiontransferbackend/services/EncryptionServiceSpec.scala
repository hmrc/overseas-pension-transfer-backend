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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import java.security.SecureRandom
import java.util.Base64

class EncryptionServiceSpec extends AnyFreeSpec with Matchers {

  private val masterKey = "test-master-key-1234567890"

  private val fixedRandom = new SecureRandom(Array.fill(16)(0.toByte))
  private val service     = new EncryptionService(masterKey, fixedRandom)

  "EncryptionService" - {

    "must encrypt and decrypt a normal string correctly" in {
      val plainText = "Hello, HMRC!"
      val encrypted = service.encrypt(plainText)
      encrypted must not be plainText

      val decrypted = service.decrypt(encrypted)
      decrypted.isRight      mustBe true
      decrypted.toOption.get mustBe plainText
    }

    "must encrypt and decrypt an empty string" in {
      val encrypted = service.encrypt("")
      val decrypted = service.decrypt(encrypted)
      decrypted.isRight      mustBe true
      decrypted.toOption.get mustBe ""
    }

    "must produce different ciphertexts for the same input due to random IV" in {
      val text       = "Repeatable text"
      val encrypted1 = service.encrypt(text)
      val encrypted2 = service.encrypt(text)

      encrypted1                                 must not be encrypted2
      service.decrypt(encrypted1).toOption.get mustBe text
      service.decrypt(encrypted2).toOption.get mustBe text
    }

    "must return Left(Throwable) when decrypting corrupted base64 input" in {
      val corrupted = "not-a-valid-base64-string"
      val result    = service.decrypt(corrupted)

      result.isLeft mustBe true
    }

    "must return Left(Throwable) when tampering with ciphertext" in {
      val text      = "Sensitive data"
      val encrypted = service.encrypt(text)
      val bytes     = Base64.getDecoder.decode(encrypted)
      bytes(bytes.length - 1) = (bytes(bytes.length - 1) ^ 0xff).toByte
      val tampered = Base64.getEncoder.encodeToString(bytes)

      service.decrypt(tampered).isLeft mustBe true
    }

    "must return Left(Throwable) when tampering with IV" in {
      val text      = "Another sensitive data"
      val encrypted = service.encrypt(text)
      val bytes     = Base64.getDecoder.decode(encrypted)
      bytes(0) = (bytes(0) ^ 0xff).toByte
      val tampered = Base64.getEncoder.encodeToString(bytes)

      service.decrypt(tampered).isLeft mustBe true
    }

    "must throw IllegalArgumentException when encrypting null" in {
      an[IllegalArgumentException] must be thrownBy service.encrypt(null)
    }

    "must handle master key in Base64 format correctly" in {
      val base64Key         = Base64.getEncoder.encodeToString(masterKey.getBytes)
      val serviceWithBase64 = new EncryptionService(base64Key, fixedRandom)

      val text      = "Base64 key test"
      val encrypted = serviceWithBase64.encrypt(text)
      val decrypted = serviceWithBase64.decrypt(encrypted)
      decrypted.isRight      mustBe true
      decrypted.toOption.get mustBe text
    }

    "must encrypt and decrypt a very long string (10MB) successfully" in {
      val longText  = "A" * (10 * 1024 * 1024) // 10 MB
      val encrypted = service.encrypt(longText)
      val decrypted = service.decrypt(encrypted)
      decrypted.isRight      mustBe true
      decrypted.toOption.get mustBe longText
    }

    "must allow multiple sequential encrypt/decrypt calls without state issues" in {
      val texts = Seq("one", "two", "three", "four", "five")
      texts.foreach { t =>
        val encrypted = service.encrypt(t)
        val decrypted = service.decrypt(encrypted)
        decrypted.isRight      mustBe true
        decrypted.toOption.get mustBe t
      }
    }
  }
}
