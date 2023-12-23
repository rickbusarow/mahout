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

package com.rickbusarow.lattice.stdlib

internal inline fun <E> Iterable<E>.splitInclusive(predicate: (E) -> Boolean): List<List<E>> {

  val toSplit = this@splitInclusive.asList()

  if (toSplit.isEmpty()) return emptyList()

  val indices = buildList {
    add(0)

    for (index in (1..toSplit.lastIndex)) {
      if (predicate(toSplit[index])) {
        add(index)
      }
    }
  }
    .distinct()

  return buildList {

    for ((i, fromIndex) in indices.withIndex()) {
      if (i == indices.lastIndex) {
        add(toSplit.drop(fromIndex).take(toSplit.size - fromIndex))
      } else {
        add(toSplit.drop(fromIndex).take(indices[i + 1] - fromIndex))
      }
    }
  }
}

/**
 * Returns a list of all elements sorted according to the specified [selectors].
 *
 * The sort is _stable_. It means that equal elements
 * preserve their order relative to each other after sorting.
 */
internal fun <T> Iterable<T>.sortedBy(vararg selectors: (T) -> Comparable<*>): List<T> {
  if (this is Collection) {
    if (size <= 1) return this.toList()
    @Suppress("UNCHECKED_CAST")
    return (toTypedArray<Any?>() as Array<T>).apply { sortWith(compareBy(*selectors)) }.asList()
  }
  return toMutableList().apply { sortWith(compareBy(*selectors)) }
}

/**
 * shorthand for `this as? Set<E> ?: toSet()`
 *
 * @return itself if the receiver [Iterable] is already a
 *   `Set<E>`, otherwise calls `toSet()` to create a new one
 */
internal fun <E> Iterable<E>.asSet(): Set<E> = this as? Set<E> ?: toSet()

/**
 * shorthand for `this as? List<E> ?: toList()`
 *
 * @return itself if the receiver [Iterable] is already a
 *   `List<E>`, otherwise calls `toList()` to create a new one
 */
internal fun <E> Iterable<E>.asList(): List<E> = this as? List<E> ?: toList()

/**
 * shorthand for `this as? Collection<E> ?: toList()`
 *
 * @return itself if the receiver [Iterable] is already a
 *   `Collection<E>`, otherwise calls `toList()` to create a new one
 */
internal fun <E> Iterable<E>.asCollection(): Collection<E> = this as? Collection<E> ?: toList()
