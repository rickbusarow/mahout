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

package com.rickbusarow.mahout.conventions

import com.rickbusarow.mahout.core.stdlib.prefixIfNot
import com.rickbusarow.mahout.libs.CatalogSection
import com.rickbusarow.mahout.libs.Node
import io.kotest.matchers.shouldBe

internal interface LibsGeneratorTestUtils {
  infix fun List<Node>.shouldBe(expected: List<Node>) {
    val actualString = joinToString("\n").prefixIfNot("\n")
    val expectedString = expected.joinToString("\n").prefixIfNot("\n")

    actualString shouldBe expectedString
  }

  fun entry(
    alias: String,
    value: String = alias.autoValue()
  ) = CatalogSection.AliasToValue(alias = alias, value = value)

  fun node(
    qualifiedName: String,
    value: String? = qualifiedName.autoValue(),
    children: ChildrenScope.() -> Unit = {}
  ) = Node(
    qualifiedName = qualifiedName,
    simpleName = qualifiedName.substringAfterLast("."),
    value = value,
    children = ChildrenScope().also { it.children() }.children
  )

  class ChildrenScope {
    val children = mutableSetOf<Node>()

    fun node(
      qualifiedName: String,
      value: String? = qualifiedName.autoValue(),
      children: ChildrenScope.() -> Unit = {}
    ) {
      this.children.add(
        Node(
          qualifiedName = qualifiedName,
          simpleName = qualifiedName.substringAfterLast("."),
          value = value,
          children = ChildrenScope().also { it.children() }.children
        )
      )
    }
  }
}

private fun String.autoValue(): String = sumOf { it.code }
  .toString(radix = 16)
  .padStart(length = 8, padChar = '0')
