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

package com.rickbusarow.lattice.core

import com.rickbusarow.kgx.getOrPut
import org.gradle.api.NamedDomainObjectCollectionSchema.NamedDomainObjectSchema
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.TaskCollection
import org.jetbrains.kotlin.gradle.plugin.extraProperties

/** @throws IllegalArgumentException if there are multiple tasks of that name when ignoring its case */
internal fun TaskCollection<*>.namedOrNull(taskName: String): NamedDomainObjectSchema? {

  // This will typically be a 1:1 grouping,
  // but Gradle does allow you to re-use task names with different capitalization,
  // like 'foo' and 'Foo'.
  val namesLowercase: Map<String, List<NamedDomainObjectSchema>> =
    (this@namedOrNull as ExtensionAware).extraProperties.getOrPut("taskNamesLowercaseToSchema") {
      collectionSchema.elements.groupBy { it.name.lowercase() }
    }

  val taskNameLowercase = taskName.lowercase()

  // All tasks that match the lowercase name
  val lowercaseMatches = namesLowercase[taskNameLowercase] ?: return null

  // The task with the same case as the requested name, or null
  val exactMatch = lowercaseMatches.singleOrNull { it.name == taskName }

  if (exactMatch != null) {
    return exactMatch
  }

  require(lowercaseMatches.size == 1) {
    "Task name '$taskName' is ambiguous.  " +
      "It matches multiple tasks: ${lowercaseMatches.map { it.name }}"
  }

  return lowercaseMatches.single()
}
