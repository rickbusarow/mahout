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

package com.rickbusarow.lattice.generator.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun <T, E> T.applyEach(elements: Iterable<E>, block: T.(e: E) -> Unit): T = apply {
  for (element in elements) {
    block(element)
  }
}

internal inline fun <T, E> T.applyEachIndexed(
  elements: Iterable<E>,
  block: T.(index: Int, e: E) -> Unit
): T = apply {
  for ((index, element) in elements.withIndex()) {
    block(index, element)
  }
}

internal inline fun <T, E> T.applyEachIndexed(
  elements: Array<E>,
  block: T.(index: Int, e: E) -> Unit
): T = apply {
  for ((index, element) in elements.withIndex()) {
    block(index, element)
  }
}

@OptIn(ExperimentalContracts::class)
internal inline fun <T> T.applyIf(predicate: Boolean, transform: T.() -> T): T {
  contract {
    callsInPlace(transform, InvocationKind.EXACTLY_ONCE)
  }
  return if (predicate) transform(this) else this
}

@OptIn(ExperimentalContracts::class)
internal inline fun <T, R : Any> T.applyIfNotNull(r: R?, transform: T.(R) -> T): T {
  contract {
    callsInPlace(transform, InvocationKind.EXACTLY_ONCE)
  }
  return if (r != null) transform(r) else this
}

internal inline fun <T> T.letIf(predicate: Boolean, transform: (T) -> T): T {
  return if (predicate) transform(this) else this
}
