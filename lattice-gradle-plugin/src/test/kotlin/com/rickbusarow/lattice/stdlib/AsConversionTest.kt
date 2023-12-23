/*
 * Copyright (C) 2023 Rick Busarow
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

package com.rickbusarow.lattice.stdlib

import com.rickbusarow.kase.Kase1
import com.rickbusarow.kase.KaseTestFactory
import com.rickbusarow.kase.TestEnvironment
import com.rickbusarow.kase.kase
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestFactory
import java.util.*

class AsConversionTest : KaseTestFactory<TestEnvironment, Kase1<Iterable<Int>>> {

  fun <E> priorityQueueOf(vararg elements: E): PriorityQueue<E> {
    return PriorityQueue<E>().also { queue ->
      elements.forEach { queue.add(it) }
    }
  }

  override val kases: List<Kase1<Iterable<Int>>>
    get() = listOf(
      kase("iterable", 1..3),
      kase("collection", priorityQueueOf(1, 2, 3)),
      kase("set", setOf(1, 2, 3)),
      kase("mutable set", mutableSetOf(1, 2, 3)),
      kase("list", listOf(1, 2, 3)),
      kase("mutable list", mutableListOf(1, 2, 3))
    )

  @Nested
  inner class `asList` {

    @TestFactory
    fun `the returned list has the same contents as the original`() = testFactory { (iterable) ->

      iterable.asList() shouldContainExactly listOf(1, 2, 3)
    }

    @TestFactory
    fun `calling asList on a list just returns the original instance`() = kases
      .filter { it.a1 is List<Int> }
      .asTests { (iterable) ->
        iterable.asList() shouldBeSameInstanceAs iterable
      }
  }

  @Nested
  inner class `asSet` {

    @TestFactory
    fun `the returned set has the same contents as the original`() = testFactory { (iterable) ->

      iterable.asSet() shouldContainExactly listOf(1, 2, 3)
    }

    @TestFactory
    fun `calling asSet on a set just returns the original instance`() = kases
      .filter { it.a1 is Set<Int> }
      .asTests { (iterable) ->
        iterable.asSet() shouldBeSameInstanceAs iterable
      }
  }

  @Nested
  inner class `asCollection` {

    @TestFactory
    fun `the returned collection has the same contents as the original`() =
      testFactory { (iterable) ->

        iterable.asCollection() shouldContainExactly listOf(1, 2, 3)
      }

    @TestFactory
    fun `calling asCollection on a collection just returns the original instance`() = kases
      .filter { it.a1 is Collection<Int> }
      .asTests { (iterable) ->
        iterable.asCollection() shouldBeSameInstanceAs iterable
      }
  }
}
