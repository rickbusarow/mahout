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

package com.rickbusarow.mahout.core

import com.rickbusarow.kgx.kotlinExtensionOrNull
import com.rickbusarow.mahout.api.GradleSourceSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

internal val Project.javaToolchainService: JavaToolchainService
  get() = extensions.getByType(JavaToolchainService::class.java)

internal val TaskContainer.clean: TaskProvider<Task>
  get() = named(LifecycleBasePlugin.CLEAN_TASK_NAME)

internal val TaskContainer.check: TaskProvider<Task>
  get() = named(LifecycleBasePlugin.CHECK_TASK_NAME)

internal typealias GradleSourceSet = org.gradle.api.tasks.SourceSet

internal fun GradleSourceSet.kotlinSourceSet(target: Project): KotlinSourceSet {
  return target.kotlinExtension.sourceSets.getByName(name)
}

internal fun GradleSourceSet.kotlinSourceSetOrNull(target: Project): KotlinSourceSet? {
  return target.kotlinExtensionOrNull?.sourceSets?.findByName(name)
}

internal fun <T, C : Collection<T>> Provider<out C>.merge(
  vararg others: Provider<out C>
): Provider<out Collection<T>> =
  when {
    others.isEmpty() -> this
    others.size == 1 -> zip(others[0]) { a, b -> a + b }
    else -> merge(others.asList())
  }

internal fun <T, C : Collection<T>> Provider<out C>.merge(
  others: Collection<Provider<out C>>
): Provider<out Collection<T>> =
  when {
    others.isEmpty() -> this
    others.size == 1 -> zip(others.first()) { a, b -> a + b }
    else -> {
      zip(others.first().merge(others.drop(1))) { a, b -> a + b }
    }
  }
