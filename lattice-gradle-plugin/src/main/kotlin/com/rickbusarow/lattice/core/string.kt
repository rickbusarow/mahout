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

import org.gradle.util.internal.TextUtil
import java.util.*

internal val SEMVER_REGEX = buildString {
  append("(?:0|[1-9]\\d*)\\.")
  append("(?:0|[1-9]\\d*)\\.")
  append("(?:0|[1-9]\\d*)")
  append("(?:-(?:(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)")
  append("(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?")
  append("(?:\\+(?:[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?")
}

/** Replaces the deprecated Kotlin version, but hard-codes `Locale.US` */
internal fun String.capitalize(): String = replaceFirstChar {
  if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
}

/**
 * Removes trailing whitespaces from all lines in a string.
 *
 * Shorthand for `lines().joinToString("\n") { it.trimEnd() }`
 */
internal fun String.trimLineEnds(): String = mapLines { it.trimEnd() }

/** performs [transform] on each line */
internal fun String.mapLines(transform: (String) -> CharSequence): String = lineSequence()
  .joinToString("\n", transform = transform)

internal fun String.normaliseLineSeparators(): String = TextUtil.convertLineSeparatorsToUnix(this)
internal fun String.prefixIfNot(
  prefix: String
) = if (this.startsWith(prefix)) this else "$prefix$this"

internal fun CharSequence.normaliseLineSeparators(): String {
  return when (this) {
    is String -> TextUtil.convertLineSeparatorsToUnix(this)
    else -> TextUtil.convertLineSeparatorsToUnix(toString())
  }
}

/** shorthand for `replace(___, "")` against multiple tokens */
internal fun String.remove(vararg strings: String): String = strings.fold(this) { acc, string ->
  acc.replace(string, "")
}

/** shorthand for `replace(___, "")` against multiple tokens */
internal fun String.remove(vararg regex: Regex): String = regex.fold(this) { acc, reg ->
  acc.replace(reg, "")
}

/**
 * example:
 *
 * ```
 * override fun toString() = buildString {
 *   appendLine("SomeClass(")
 *   indent {
 *     appendLine("prop1=$prop1")
 *     appendLine("prop2=$prop2")
 *   }
 *   appendLine(")")
 * }
 * ```
 */
public inline fun StringBuilder.indent(
  leadingIndent: String = "  ",
  continuationIndent: String = leadingIndent,
  builder: StringBuilder.() -> Unit
) {

  val inner = buildString {
    append(leadingIndent)

    builder()
  }

  if (inner.isBlank()) return

  append(inner.prependContinuationIndent(continuationIndent))
}

/**
 * Prepends [continuationIndent] to every line of the original string.
 *
 * Doesn't preserve the original line endings.
 */
public fun CharSequence.prependContinuationIndent(
  continuationIndent: String,
  skipBlankLines: Boolean = true
): String = mapLinesIndexed { i, line ->
  when {
    i == 0 -> line
    skipBlankLines && line.isBlank() -> line
    else -> "$continuationIndent$line"
  }
}

/**
 * Adds line breaks and indents to the output of data class `toString()`s.
 *
 * @see toStringPretty
 */
public fun String.prettyToString(): String {
  return replace(",", ",\n")
    .replace("(", "(\n")
    .replace(")", "\n)")
    .replace("[", "[\n")
    .replace("]", "\n]")
    .replace("{", "{\n")
    .replace("}", "\n}")
    .replace("\\(\\s*\\)".toRegex(), "()")
    .replace("\\[\\s*]".toRegex(), "[]")
    .indentByBrackets()
    .replace("""\n *\n""".toRegex(), "\n")
}

/**
 * shorthand for `toString().prettyToString()`, which adds line breaks and indents to a string
 *
 * @see prettyToString
 */
public fun Any?.toStringPretty(): String = when (this) {
  is Map<*, *> -> toList().joinToString("\n")
  else -> toString().prettyToString()
}

/** A naive auto-indent which just counts brackets. */
public fun String.indentByBrackets(tab: String = "  "): String {

  var tabCount = 0

  val open = setOf('{', '(', '[', '<')
  val close = setOf('}', ')', ']', '>')

  return lines()
    .map { it.trim() }
    .joinToString("\n") { line ->

      if (line.firstOrNull() in close) {
        tabCount--
      }

      "${tab.repeat(tabCount)}$line"
        .also {

          // Arrows aren't brackets
          val noSpecials = line.remove("<=", "->")

          tabCount += noSpecials.count { char -> char in open }
          // Skip the first char because if it's a closing bracket, it was already counted above.
          tabCount -= noSpecials.drop(1).count { char -> char in close }
        }
    }
}

/**
 * Creates a string from all the elements separated using [separator]
 * and using the given [prefix] and [postfix] if supplied.
 *
 * If the collection could be huge, you can specify a non-negative value
 * of [limit], in which case only the first [limit] elements will be
 * appended, followed by the [truncated] string (which defaults to "...").
 */
public fun <T> List<T>.joinToStringIndexed(
  separator: CharSequence = ", ",
  prefix: CharSequence = "",
  postfix: CharSequence = "",
  limit: Int = -1,
  truncated: CharSequence = "...",
  transform: (Int, T) -> CharSequence
): String {
  return buildString {
    append(prefix)
    var count = 0
    for (element in this@joinToStringIndexed) {
      if (++count > 1) append(separator)
      if (limit < 0 || count <= limit) {
        append(transform(count - 1, element))
      } else {
        break
      }
    }
    if (limit in 0 until count) append(truncated)
    append(postfix)
  }
}

/**
 * shorthand for `replaceIndent(" ".repeat(numSpaces))`
 *
 * @see kotlin.text.replaceIndent
 */
public fun String.replaceIndent(numSpaces: Int): String {
  return replaceIndent(" ".repeat(numSpaces))
}

/**
 * performs [transform] on each line
 *
 * Doesn't preserve the original line endings.
 */
public fun CharSequence.mapLines(transform: (String) -> CharSequence): String = lineSequence()
  .joinToString("\n", transform = transform)

/**
 * performs [transform] on each line
 *
 * Doesn't preserve the original line endings.
 */
public fun CharSequence.mapLinesIndexed(transform: (Int, String) -> CharSequence): String =
  lineSequence()
    .mapIndexed(transform)
    .joinToString("\n")
