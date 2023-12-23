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

internal class SimpleMultimap<K, V> : Serializable {
  private val map: MutableMap<K, MutableSet<V>> = HashMap()

  val keys: Set<K> get() = map.keys
  val size: Int get() = map.size
  val values: Collection<Set<V>> get() = map.values

  operator fun get(key: K): Set<V>? = map[key]?.toSet()
  fun getOrEmpty(key: K): Set<V> = get(key).orEmpty()
  operator fun contains(key: K): Boolean = map.containsKey(key)

  fun contains(key: K, value: V): Boolean = map[key]?.contains(value) ?: false

  fun add(key: K, value: V): Boolean = map.getOrPut(key) { mutableSetOf() }.add(value)
  fun addAll(
    key: K,
    values: Iterable<V>
  ): Boolean = map.getOrPut(key) { mutableSetOf() }.addAll(values)

  fun remove(key: K): Set<V>? = map.remove(key)?.toSet()
  fun remove(key: K, value: V): Boolean {
    val values = map[key] ?: return false

    val removed = values.remove(value)
    if (values.isEmpty()) {
      map.remove(key)
    }
    return removed
  }

  fun isEmpty(): Boolean = map.isEmpty()

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
