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

package com.rickbusarow.mahout.conventions

import com.diffplug.gradle.spotless.SpotlessApply
import com.rickbusarow.ktlint.KtLintFormatTask
import com.rickbusarow.mahout.api.DefaultMahoutCheckTask
import com.rickbusarow.mahout.api.DefaultMahoutFixTask
import com.rickbusarow.mahout.api.MahoutFixTask
import com.rickbusarow.mahout.deps.PluginIds
import kotlinx.validation.KotlinApiBuildTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.reflect.TypeOf
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.language.base.plugins.LifecycleBasePlugin

/** */
public abstract class FixPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    target.plugins.apply("base")

    val fix = target.tasks.register("fix", DefaultMahoutFixTask::class.java) { task ->

      task.group = "Verification"
      task.description = "Runs all auto-fix linting tasks"

      task.dependsOn(
        target.tasks.apiDump(),
        target.tasks.withType(SpotlessApply::class.java),
        target.tasks.withType(KotlinApiBuildTask::class.java),
        target.tasks.withType(DeleteEmptyDirsTask::class.java),
        target.tasks.withType(KtLintFormatTask::class.java),
        target.tasks.withType(MahoutFixTask::class.java).named { it != "fix" }
      )

      target.plugins.withId(PluginIds.`dropbox-dependency-guard`) {
        task.dependsOn(target.tasks.named("dependencyGuardBaseline"))
      }
      target.plugins.withId(PluginIds.`rickBusarow-moduleCheck`) {
        task.dependsOn(target.tasks.named("moduleCheckAuto"))

        // val mcCheckTasks = target.tasks
        //   .withType(AbstractModuleCheckTask::class.java)
        //   .providers(target.providers) { !it.name.endsWith("Auto") }
        //
        // target.tasks.withType(AbstractModuleCheckTask::class.java)
        //   .configureEach { task ->
        //     if (task.name.endsWith("Auto")) task.mustRunAfter(mcCheckTasks)
        //   }
      }
    }

    // This is a convenience task which applies all available fixes before running `check`. Each
    // of the fixable linters use `mustRunAfter` to ensure that their auto-fix task runs before their
    // check-only task.
    target.tasks.register("checkFix", DefaultMahoutCheckTask::class.java) { task ->

      task.group = "Verification"
      task.description = "Runs all auto-fix linting tasks, then runs all of the normal :check task"

      task.dependsOn(target.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME))
      task.dependsOn(fix)
    }
  }
}

internal fun <T : Task> TaskCollection<T>.namedFromSchema(
  target: ProviderFactory,
  predicate: (name: String, publicType: TypeOf<*>) -> Boolean
): Provider<List<TaskProvider<T>>> = target.provider {
  collectionSchema.elements
    .filter { predicate(it.name, it.publicType) }
    .map { named(it.name) }
}

/** */
public fun <T : Task> TaskCollection<T>.providers(
  providerFactory: ProviderFactory
): Provider<List<TaskProvider<T>>> =
  providerFactory.provider { collectionSchema.elements.map { named(it.name) } }

/** */
public fun <T : Task> TaskCollection<T>.providers(
  providerFactory: ProviderFactory,
  predicate: (TaskProvider<T>) -> Boolean
): Provider<List<TaskProvider<T>>> = providers(providerFactory)
  .map { it.filter(predicate) }
