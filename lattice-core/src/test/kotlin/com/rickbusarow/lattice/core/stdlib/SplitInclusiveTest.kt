/*
 * Copyright (C) 2024 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rickbusarow.lattice.core.stdlib

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SplitInclusiveTest {

  @Test
  fun `returns a single sublist with all elements if none satisfy the predicate`() {

    val list = listOf(1, 3, 5, 7, 9)

    list.splitInclusive { it % 2 == 0 } shouldBe listOf(listOf(1, 3, 5, 7, 9))
  }

  @Test
  fun `elements that satisfy the predicate are the start of each sub-list`() {

    val list = listOf(0, 2, 4, 5, 6, 1, 1, 1, 8, 0, 1, 3, 5)

    list.splitInclusive { it % 2 == 0 } shouldBe listOf(
      listOf(0),
      listOf(2),
      listOf(4, 5),
      listOf(6, 1, 1, 1),
      listOf(8),
      listOf(0, 1, 3, 5)
    )
  }

  @Test
  fun `the first sublist contains elements before the first matched element`() {

    val list = listOf(1, 3, 5, 7, 0, 1, 2, 2, 3, 4)

    list.splitInclusive { it % 2 == 0 } shouldBe listOf(
      listOf(1, 3, 5, 7),
      listOf(0, 1),
      listOf(2),
      listOf(2, 3),
      listOf(4)
    )
  }

  @Test
  fun `splitInclusive should split list based on predicate`() {
    val list = listOf(1, 2, 3, 4, 5, 6)
    val result = list.splitInclusive { it % 2 == 0 }
    result shouldBe listOf(
      listOf(1),
      listOf(2, 3),
      listOf(4, 5),
      listOf(6)
    )
  }

  @Test
  fun `splitInclusive should return empty list when input list is empty`() {
    val list = emptyList<Int>()
    val result = list.splitInclusive { it % 2 == 0 }
    result shouldBe emptyList()
  }
}
