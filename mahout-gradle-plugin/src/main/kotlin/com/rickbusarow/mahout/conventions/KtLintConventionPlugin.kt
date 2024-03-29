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

package com.rickbusarow.mahout.conventions

import com.rickbusarow.ktlint.KtLintPlugin
import com.rickbusarow.ktlint.KtLintTask
import com.rickbusarow.mahout.core.VERSION_NAME
import com.rickbusarow.mahout.deps.Libs
import kotlinx.validation.KotlinApiBuildTask
import kotlinx.validation.KotlinApiCompareTask
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UndocumentedPublicClass")
public abstract class KtLintConventionPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    target.plugins.apply(KtLintPlugin::class.java)

    target.dependencies
      .add("ktlint", Libs.`rickBusarow-ktrules`)

    target.tasks.withType(KtLintTask::class.java).configureEach { task ->
      System.setProperty("ktrules.project_version", target.VERSION_NAME)

      task.mustRunAfter(
        target.tasks.namedFromSchema(target.providers) { name, _ ->
          name == "dependencyGuardBaseline" || name == "dependencyGuard"
        },
        target.tasks.withType(KotlinApiBuildTask::class.java),
        target.tasks.withType(KotlinApiCompareTask::class.java)
      )
    }
  }
}
