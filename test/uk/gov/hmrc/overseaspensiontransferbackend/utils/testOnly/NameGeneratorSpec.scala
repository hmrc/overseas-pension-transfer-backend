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

package uk.gov.hmrc.overseaspensiontransferbackend.utils.testOnly

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class NameGeneratorSpec extends AnyFreeSpec with Matchers {

  "nameFor" - {

    "returns the same name for the same (pstr, i) across invocations" in {
      val pstr = "S2400000001"
      val i    = 6
      val a    = NameGenerator.nameFor(pstr, i)
      val b    = NameGenerator.nameFor(pstr, i)
      a mustBe b
    }

    "returns the same sequence for a range of i when called twice" in {
      val pstr = "S2400000001"
      val n    = 200
      val seq1 = (1 to n).map(i => NameGenerator.nameFor(pstr, i)).toVector
      val seq2 = (1 to n).map(i => NameGenerator.nameFor(pstr, i)).toVector
      seq1 mustBe seq2
    }

    "generally differs for different PSTRs" in {
      val pstr1 = "S2400000001"
      val pstr2 = "S2400000002"
      val n     = 100
      val seq1  = (1 to n).map(i => NameGenerator.nameFor(pstr1, i)).toVector
      val seq2  = (1 to n).map(i => NameGenerator.nameFor(pstr2, i)).toVector

      seq1 must not be seq2

      val sameCount = seq1.zip(seq2).count { case (a, b) => a == b }
      sameCount must be < n
    }

    "produces well-formed hyphenated surnames when they occur" in {
      val pstr     = "S2400000001"
      val n        = 1000
      val hyphened = (1 to n)
        .iterator
        .map(i => NameGenerator.nameFor(pstr, i)._2)
        .find(_.contains("-"))

      hyphened.foreach { last =>
        last.count(_ == '-') mustBe 1
        val parts = last.split("-", 2)
        withClue(s"hyphenated last name not well-formed: $last") {
          parts.length           mustBe 2
          parts(0).trim.nonEmpty mustBe true
          parts(1).trim.nonEmpty mustBe true
        }
      }
      succeed
    }
  }
}
