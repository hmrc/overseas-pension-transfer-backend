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

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.{MessageDigest, SecureRandom}
import java.util.Base64
import javax.crypto.{Cipher, SecretKey}
import javax.crypto.spec.{GCMParameterSpec, SecretKeySpec}

import scala.util.{Failure, Success, Try}

class EncryptionService(masterKey: String, random: SecureRandom = new SecureRandom()) {

  private val AES_ALGO        = "AES/GCM/NoPadding"
  private val GCM_TAG_BITS    = 128
  private val IV_LENGTH_BYTES = 12

  private val key: SecretKey = deriveKey(masterKey)

  private def deriveKey(secret: String): SecretKey = {
    val decoded = tryBase64DecodeOrPlain(secret)
    val digest  = MessageDigest.getInstance("SHA-256").digest(decoded)
    new SecretKeySpec(digest, "AES")
  }

  private def tryBase64DecodeOrPlain(value: String): Array[Byte] =
    Try(Base64.getDecoder.decode(value)) match {
      case Success(bytes) => bytes
      case Failure(_)     => value.getBytes(StandardCharsets.UTF_8)
    }

  def encrypt(plainText: String): String = {
    require(plainText != null, "Cannot encrypt null string")

    val iv = new Array[Byte](IV_LENGTH_BYTES)
    random.nextBytes(iv)

    val cipher = Cipher.getInstance(AES_ALGO)
    cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv))

    val cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8))
    val payload    = ByteBuffer.allocate(iv.length + cipherText.length).put(iv).put(cipherText).array()

    Base64.getEncoder.encodeToString(payload)
  }

  def decrypt(encoded: String): Either[Throwable, String] = {
    Try {
      val payload = Base64.getDecoder.decode(encoded)
      val buf     = ByteBuffer.wrap(payload)

      val iv = new Array[Byte](IV_LENGTH_BYTES)
      buf.get(iv)

      val cipherBytes = new Array[Byte](payload.length - IV_LENGTH_BYTES)
      buf.get(cipherBytes)

      val cipher = Cipher.getInstance(AES_ALGO)
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv))

      new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8)
    }.toEither
  }
}
