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

import com.rickbusarow.lattice.core.InternalLatticeApi
import org.gradle.util.internal.TextUtil
import java.util.*

/**
 * A regular expression for matching semantic version strings.
 *
 * The regex is based on the [Semantic Versioning 2.0.0](https://semver.org/) specification.
 */
@InternalLatticeApi
public val SEMVER_REGEX: String = buildString {
  append("(?:0|[1-9]\\d*)\\.")
  append("(?:0|[1-9]\\d*)\\.")
  append("(?:0|[1-9]\\d*)")
  append("(?:-(?:(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)")
  append("(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?")
  append("(?:\\+(?:[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?")
}

/** Replaces the deprecated Kotlin version, but hard-codes `Locale.US` */
@InternalLatticeApi
public fun String.capitalize(): String = replaceFirstChar {
  if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
}

/**
 * Removes trailing whitespaces from all lines in a string.
 *
 * Shorthand for `lines().joinToString("\n") { it.trimEnd() }`
 */
@InternalLatticeApi
public fun String.trimLineEnds(): String = mapLines { it.trimEnd() }

/** performs [transform] on each line */
@InternalLatticeApi
public fun String.mapLines(transform: (String) -> CharSequence): String =
  lineSequence()
    .joinToString("\n", transform = transform)

/**
 * Converts all line separators in the specified non-null string to the Unix line separator `\n`.
 */
@InternalLatticeApi
public fun String.normaliseLineSeparators(): String =
  TextUtil.convertLineSeparatorsToUnix(this)

/**
 * Converts all line separators in the specified non-null string to the Unix line separator `\n`.
 */
@InternalLatticeApi
public fun CharSequence.normaliseLineSeparators(): String {
  return when (this) {
    is String -> TextUtil.convertLineSeparatorsToUnix(this)
    else -> TextUtil.convertLineSeparatorsToUnix(toString())
  }
}

/** Adds [prefix] to the beginning of the string if it's not already there. */
@InternalLatticeApi
public fun String.prefixIfNot(prefix: String): String = if (this.startsWith(prefix)) {
  this
} else {
  "$prefix$this"
}

/** shorthand for `replace(___, "")` against multiple tokens */
@InternalLatticeApi
public fun String.remove(vararg strings: String): String =
  strings.fold(this) { acc, string ->
    acc.replace(string, "")
  }

/** shorthand for `replace(___, "")` against multiple tokens */
@InternalLatticeApi
public fun String.remove(vararg regex: Regex): String =
  regex.fold(this) { acc, reg ->
    acc.replace(reg, "")
  }
