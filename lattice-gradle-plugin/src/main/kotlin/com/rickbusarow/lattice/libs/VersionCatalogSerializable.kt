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

package com.rickbusarow.lattice.libs

import com.rickbusarow.kgx.library
import com.rickbusarow.kgx.pluginId
import com.rickbusarow.kgx.version
import com.rickbusarow.lattice.core.indent
import com.rickbusarow.lattice.core.stdlib.takeView
import org.gradle.api.artifacts.VersionCatalog

/** */
public class VersionCatalogSerializable(versionCatalog: VersionCatalog) : java.io.Serializable {

  /** */
  public val name: String = versionCatalog.name

  /** */
  public val versions: List<Pair<String, String>> = versionCatalog.versionAliases
    .sorted()
    .filterNot { it.startsWith("config.") }
    .map { alias -> alias to versionCatalog.version(alias) }

  /** */
  public val plugins: List<Pair<String, String>> = versionCatalog.pluginAliases
    .sorted()
    .map { alias -> alias to versionCatalog.pluginId(alias) }

  /** */
  public val libraries: List<Pair<String, String>> = versionCatalog.libraryAliases
    .sorted()
    .map { alias -> alias to versionCatalog.library(alias).get().toString() }

  /** */
  public val bundles: List<Pair<String, String>> = versionCatalog.bundleAliases
    .sorted()
    .map { alias -> alias to versionCatalog.findBundle(alias).get().toString() }
}

internal class Node(
  val qualifiedName: String,
  val simpleName: String,
  var value: String? = null,
  val children: MutableSet<Node> = mutableSetOf()
) : Comparable<Node> {
  override fun compareTo(other: Node): Int = simpleName.compareTo(other.simpleName)

  override fun toString(): String = buildString {
    append("$qualifiedName | $simpleName | $value")
    if (children.isNotEmpty()) {
      appendLine()
      indent("    ") {
        for (child in children.sorted()) {
          appendLine(child.toString().trim())
        }
      }
    }
  }.trim()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Node) return false

    if (qualifiedName != other.qualifiedName) return false

    return true
  }

  override fun hashCode(): Int {
    return qualifiedName.hashCode()
  }
}

internal data class CatalogSection(val aliasToValue: List<AliasToValue>) {

  private val map = aliasToValue.associate { it.alias to it.value }
  val aliases: List<String> = map.keys.sorted()

  operator fun get(alias: String): String? = map[alias]

  internal fun asNodes(): List<Node> {

    val m = mutableMapOf<String, Node>()
    fun node(qualified: String, simple: String) = m.getOrPut(qualified) {
      Node(
        qualifiedName = qualified,
        simpleName = simple
      )
    }

    val pathSegmentsToChildPathSegments = mutableMapOf<List<String>, MutableSet<List<String>>>()
    fun MutableMap<List<String>, MutableSet<List<String>>>.add(
      parent: List<String>,
      child: List<String>?
    ) {
      getOrPut(parent) { mutableSetOf() }
        .also { if (child != null) it.add(child) }
    }

    for ((alias, _) in aliasToValue) {
      val parts = alias.split(".")

      repeat(parts.size) { index ->
        val parentNum = index + 1
        val childNum = index + 2
        val childSegments = if (parts.size >= childNum) parts.takeView(childNum) else null

        pathSegmentsToChildPathSegments.add(parts.takeView(parentNum), childSegments)
      }
    }

    fun nodeWithChildren(parentSegments: List<String>, childrenSegments: Set<List<String>>): Node {

      return node(
        qualified = parentSegments.joinToString("."),
        simple = parentSegments.last()
      )
        .also { root ->

          val childNodes = childrenSegments.map { segs ->
            nodeWithChildren(
              parentSegments = segs,
              childrenSegments = pathSegmentsToChildPathSegments[segs].orEmpty()
            )
          }
          root.children.addAll(childNodes)
          root.value = get(root.qualifiedName)
        }
    }

    return pathSegmentsToChildPathSegments
      // only create top-level nodes for single-segment aliases
      .filter { it.key.size == 1 }
      .map { (parentSegments, allChildrenSegments) ->

        nodeWithChildren(
          parentSegments = parentSegments,
          childrenSegments = allChildrenSegments
        )
      }
  }

  data class AliasToValue(
    val alias: String,
    val value: String
  ) : Comparable<AliasToValue> {
    override fun compareTo(other: AliasToValue): Int = alias.compareTo(other.alias)
  }
}
