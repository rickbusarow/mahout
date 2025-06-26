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

package com.rickbusarow.mahout.core

import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.config.url
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**  */
internal val Project.VERSION_NAME: String
  get() = mahoutProperties.versionName.get()

/**  */
internal val Project.versionIsSnapshot: Boolean
  get() = VERSION_NAME.endsWith("-SNAPSHOT")

/** ex: `square` */
internal val Project.GITHUB_OWNER: String
  get() = mahoutProperties.repository.github.owner.get()

/** ex: `square/logcat` */
internal val Project.GITHUB_OWNER_REPO: String
  get() = mahoutProperties.repository.github.repo.get()

/** ex: `https://github.com/square/okio` */
internal val Project.GITHUB_REPOSITORY: String
  get() = mahoutProperties.repository.github.url.get()

/**
 * Finds a property that's prefixed with the 'commonPrefix'
 * property, like `mahout.allow-maven-local`.
 *
 * @return the property value, or null if it doesn't exist
 * @see prefixedProperty for a non-nullable version
 */
internal inline fun <reified T> Project.prefixedPropertyOrNull(name: String): T? {
  return findProperty(getPrefixedPropertyName(name)) as? T
}

/**
 * Finds a property that's prefixed with the 'commonPrefix'
 * property, like `mahout.allow-maven-local`.
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
 * The common prefix used for all project properties, like `mahout` in `mahout.allow-maven-local`.
 */
internal val Project.commonPropertyPrefix: String
  get() = (findProperty("commonPrefix") ?: group) as String

/**
 * Sets the property to the given value if it is not already set.
 */
internal fun <T : Any> Property<T>.setIfNull(value: T) {
  if (!isPresent) set(value)
}

/**
 * Sets the property to the given value if it is not already set.
 */
internal fun <T : Any> Property<T>.setIfNull(value: Provider<T>) {
  if (!isPresent) set(value)
}
