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

import com.rickbusarow.kgx.extras
import com.rickbusarow.kgx.getOrPut
import com.rickbusarow.lattice.config.latticeProperties
import com.rickbusarow.lattice.config.url
import org.gradle.api.Project

/**  */
public val Project.VERSION_NAME: String
  get() = latticeProperties.versionName.get()

/**  */
public val Project.versionIsSnapshot: Boolean
  get() = extras.getOrPut("versionIsSnapshot") { VERSION_NAME.endsWith("-SNAPSHOT") }

/** ex: `square` */
public val Project.GITHUB_OWNER: String
  get() = latticeProperties.repository.github.owner.get()

/** ex: `square/logcat` */
public val Project.GITHUB_OWNER_REPO: String
  get() = latticeProperties.repository.github.repo.get()

/** ex: `https://github.com/square/okio` */
public val Project.GITHUB_REPOSITORY: String
  get() = latticeProperties.repository.github.url.get()

/**
 * Finds a property that's prefixed with the 'commonPrefix'
 * property, like `lattice.allow-maven-local`.
 *
 * @return the property value, or null if it doesn't exist
 * @see prefixedProperty for a non-nullable version
 */
internal inline fun <reified T> Project.prefixedPropertyOrNull(name: String): T? {
  return findProperty(getPrefixedPropertyName(name)) as? T
}

/**
 * Finds a property that's prefixed with the 'commonPrefix'
 * property, like `lattice.allow-maven-local`.
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
 * The common prefix used for all project properties, like `lattice` in `lattice.allow-maven-local`.
 */
internal val Project.commonPropertyPrefix: String
  get() = (findProperty("commonPrefix") ?: group) as String
