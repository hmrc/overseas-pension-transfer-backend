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

import java.util.concurrent.ThreadLocalRandom

object NameGenerator {

  private val firstNames: Vector[String] = Vector(
    "Ada",
    "Alan",
    "Amir",
    "Anika",
    "Ava",
    "Beth",
    "Carlos",
    "Chloe",
    "Dmitri",
    "Edith",
    "Ethan",
    "Farah",
    "Grace",
    "Hannah",
    "Ian",
    "Ines",
    "Jamal",
    "Jean",
    "Kaito",
    "Ken",
    "Laura",
    "Liam",
    "Maya",
    "Mei",
    "Nadia",
    "Noah",
    "Omar",
    "Owen",
    "Priya",
    "Quinn",
    "Ravi",
    "Rosa",
    "Sam",
    "Sara",
    "Tariq",
    "Tess",
    "Uma",
    "Victor",
    "Will",
    "Xena",
    "Yara",
    "Yuki",
    "Zach",
    "Zoë",
    "Álvaro",
    "Émile",
    "Łukasz",
    "Søren",
    "Åsa",
    "José"
  )

  private val lastNames: Vector[String] = Vector(
    "Lovelace",
    "Turing",
    "Hopper",
    "Okafor",
    "Ishikawa",
    "Östberg",
    "O'Connor",
    "García",
    "Ndiaye",
    "Kowalski",
    "Schröder",
    "Fernández",
    "D'Alessandro",
    "Novák",
    "Popescu",
    "Müller",
    "Nakamura",
    "Hernández",
    "Silva",
    "Bălan",
    "Nowak",
    "Ibrahim",
    "Singh",
    "Gupta",
    "Kim",
    "Park",
    "Nguyen",
    "Hassan",
    "Ali",
    "Johnson",
    "Brown",
    "Taylor",
    "Anderson",
    "Thomas",
    "Jackson",
    "White",
    "Harris",
    "Martin",
    "Thompson",
    "Clark"
  )

  /** Deterministically pick a first/last name and maybe hyphenate the surname. */
  def nameFor(pstr: String, i: Int): (String, String) = {
    val seed = (pstr.##.toLong << 32) ^ i.toLong
    val r    = new java.util.Random(seed)

    val first = firstNames(r.nextInt(firstNames.length))
    val last1 = lastNames(r.nextInt(lastNames.length))

    // 20% chance to hyphenate with a second surname
    val last =
      if (r.nextDouble() < 0.20d) {
        val last2 = lastNames(r.nextInt(lastNames.length))
        if (last2 == last1) {
          last1
        } else {
          s"$last1-$last2"
        }
      } else {
        last1
      }

    (first, last)
  }
}
