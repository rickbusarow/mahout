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

package com.rickbusarow.antipasto.core

import com.rickbusarow.kgx.extras
import com.rickbusarow.kgx.getOrPut
import org.gradle.api.Project

/**  */
public val Project.VERSION_NAME: String
  get() = property("VERSION_NAME") as String

/**  */
public val Project.versionIsSnapshot: Boolean
  get() = extras.getOrPut("versionIsSnapshot") { VERSION_NAME.endsWith("-SNAPSHOT") }

/**  */
public val Project.GROUP: String
  get() = property("GROUP") as String

/** "1.6", "1.7", "1.8", etc. */
public val Project.KOTLIN_API: String
  get() = property("KOTLIN_API") as String

/** ex: `square` */
public val Project.GITHUB_OWNER: String
  get() = property("GITHUB_OWNER") as String

/** ex: `square/logcat` */
public val Project.GITHUB_OWNER_REPO: String
  get() = property("GITHUB_OWNER_REPO") as String

/** ex: `https://github.com/square/okio` */
public val Project.GITHUB_REPOSITORY: String
  get() = property("GITHUB_REPOSITORY") as String

/**
 * the jdk used in packaging
 *
 * "1.6", "1.8", "11", etc.
 */
public val Project.JVM_TARGET: String
  get() = property("JVM_TARGET") as String

/** `6`, `8`, `11`, etc. */
public val Project.JVM_TARGET_INT: Int
  get() = JVM_TARGET.substringAfterLast('.').toInt()

/**
 * the jdk used to build the project
 *
 * "1.6", "1.8", "11", etc.
 */
public val Project.JDK: String
  get() = property("JDK") as String

/**
 * the jdk used to build the project
 *
 * "1.6", "1.8", "11", etc.
 */
internal val Project.JDK_INT: Int
  get() = JDK.substringAfterLast('.').toInt()

/**
 * Finds a property that's prefixed with the 'commonPrefix'
 * property, like `antipasto.allow-maven-local`.
 *
 * @return the property value, or null if it doesn't exist
 * @see prefixedProperty for a non-nullable version
 */
internal inline fun <reified T> Project.prefixedPropertyOrNull(name: String): T? {
  return findProperty(getPrefixedPropertyName(name)) as? T
}

/**
 * Finds a property that's prefixed with the 'commonPrefix'
 * property, like `antipasto.allow-maven-local`.
 *
 * @return the property value
 * @see prefixedPropertyOrNull for a nullable version
 * @throws IllegalStateException if the property doesn't exist
 */
internal inline fun <reified T> Project.prefixedProperty(name: String): T {
  return property(getPrefixedPropertyName(name)) as T
}

@PublishedApi
internal fun Project.getPrefixedPropertyName(propertySuffix: String): String {
  return "$commonPropertyPrefix.$propertySuffix"
}

/**
 * The common prefix used for all project properties,
 * like `antipasto` in `antipasto.allow-maven-local`.
 */
internal val Project.commonPropertyPrefix: String
  get() = (findProperty("commonPrefix") ?: group) as String
