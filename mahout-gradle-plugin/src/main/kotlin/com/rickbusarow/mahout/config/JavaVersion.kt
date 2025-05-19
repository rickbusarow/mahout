/*
 * Copyright (C) 2025 Rick Busarow
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

package com.rickbusarow.mahout.config

import com.rickbusarow.kgx.fromInt
import org.gradle.api.provider.Provider
import org.gradle.jvm.toolchain.JavaLanguageVersion
import java.io.Serializable
import org.gradle.api.JavaVersion as GradleJavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget as JvmTargetKotlinGradle

/**
 * Wraps a String JDK version value (e.g. `"1.8"`, `"11"`, `"17"`,
 * etc.) and provides conversions to all the other necessary formats.
 *
 * @property version The JDK version as a String (e.g. `"1.8"`, `"11"`, `"17"`, etc.)
 */
@JvmInline
public value class JavaVersion(public val version: String) : Serializable, Comparable<JavaVersion> {
  /**
   * The `Int` representation of [version]. For String values like `"1.8"`, this value will be `8`.
   */
  public val major: Int get() = version.substringAfterLast('.').toInt()

  /** The [JavaLanguageVersion] representation of [version]. */
  public val javaLanguageVersion: JavaLanguageVersion get() = JavaLanguageVersion.of(major)

  /** The [org.gradle.api.JavaVersion] representation of [version]. */
  public val javaVersionGradle: GradleJavaVersion get() = GradleJavaVersion.toVersion(version)

  /** The [org.jetbrains.kotlin.gradle.dsl.JvmTarget] representation of [version]. */
  public val jvmTargetKotlinGradle: JvmTargetKotlinGradle
    get() = JvmTargetKotlinGradle.fromInt(major)

  override fun compareTo(other: JavaVersion): Int = version.compareTo(other.version)

  public companion object {

    internal val Provider<JavaVersion>.version: Provider<String> get() = map { it.version }
    internal val Provider<JavaVersion>.major: Provider<Int> get() = map { it.major }
    internal val Provider<JavaVersion>.javaLanguageVersion: Provider<JavaLanguageVersion>
      get() = map { it.javaLanguageVersion }
    internal val Provider<JavaVersion>.javaVersionGradle: Provider<GradleJavaVersion>
      get() = map { it.javaVersionGradle }
    internal val Provider<JavaVersion>.jvmTargetKotlinGradle: Provider<JvmTargetKotlinGradle>
      get() = map { it.jvmTargetKotlinGradle }
  }
}
