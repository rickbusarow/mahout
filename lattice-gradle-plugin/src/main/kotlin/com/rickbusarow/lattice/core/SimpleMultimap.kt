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

package com.rickbusarow.lattice.core

import java.io.Serializable

/** A simple Multimap implementation that's mostly a wrapper around a `Map<K, Set<V>>`. */
@InternalLatticeApi
public class SimpleMultimap<K, V> : Serializable {
  private val map: MutableMap<K, MutableSet<V>> = HashMap()

  /** All keys in the map. */
  public val keys: Set<K> get() = map.keys

  /** The number of key-value pairs in the map. */
  public val size: Int get() = map.size

  /** All sets of values in the map. */
  public val values: Collection<Set<V>> get() = map.values

  /**
   * Returns the set of values associated with the given
   * key, or an empty set if the key is not present.
   */
  public operator fun get(key: K): Set<V> = map[key].orEmpty()

  /** True if the key is present in the map. */
  public operator fun contains(key: K): Boolean = map.containsKey(key)

  /** True if the [key] is present in the map and [value] is in its set of values. */
  public fun contains(key: K, value: V): Boolean = map[key]?.contains(value) ?: false

  /**
   * Adds [value] to the set of values associated with [key].
   *
   * @return `true` if the set value set for [key] changed as a result of this operation.
   */
  public fun add(key: K, value: V): Boolean = map.getOrPut(key) { mutableSetOf() }.add(value)

  /**
   * Adds all [values] to the set of values associated with [key].
   *
   * @return `true` if the set value set for [key] changed as a result of this operation.
   */
  public fun addAll(
    key: K,
    values: Iterable<V>
  ): Boolean {
    if (!values.iterator().hasNext()) return true
    return map.getOrPut(key) { mutableSetOf() }.addAll(values)
  }

  /**
   * Adds all [values] to the set of values associated with [key].
   *
   * @return `true` if the set value set for [key] changed as a result of this operation.
   */
  public fun addAll(
    key: K,
    value: V,
    vararg additionalValues: V
  ): Boolean = addAll(key, listOf(value) + additionalValues)

  /**
   * Removes all values associated with [key].
   *
   * @return the set of values previously associated with [key], or `null` if [key] was not present.
   */
  public fun remove(key: K): Set<V>? = map.remove(key)?.toSet()

  /**
   * Removes [value] from the set of values associated with [key].
   *
   * @return `true` if [value] was present in the set of values associated with [key].
   */
  public fun remove(key: K, value: V): Boolean {
    val values = map[key] ?: return false

    val removed = values.remove(value)
    if (values.isEmpty()) {
      map.remove(key)
    }
    return removed
  }

  /** @return `true` if there are no keys in the map */
  public fun isEmpty(): Boolean = map.isEmpty()

  override fun toString(): String = map.entries.joinToString("\n") { "${it.key} : ${it.value}" }
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SimpleMultimap<*, *>) return false

    if (map != other.map) return false

    return true
  }

  override fun hashCode(): Int {
    return map.hashCode()
  }
}
