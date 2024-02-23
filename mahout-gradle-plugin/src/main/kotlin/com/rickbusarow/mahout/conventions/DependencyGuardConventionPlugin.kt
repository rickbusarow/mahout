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

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPluginExtension
import com.rickbusarow.kgx.applyOnce
import com.rickbusarow.kgx.isRootProject
import com.rickbusarow.kgx.withJavaBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin

/** */
public abstract class DependencyGuardConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {

    if (target.isRootProject()) return

    target.plugins.applyOnce("com.dropbox.dependency-guard")

    target.plugins.withJavaBasePlugin {
      configureDependencyGuard(target)
    }
  }

  private fun configureDependencyGuard(target: Project) {
    target.extensions.configure(DependencyGuardPluginExtension::class.java) { extension ->
      extension.configuration("runtimeClasspath") {
        it.modules = true
      }
    }

    target.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
      it.dependsOn("dependencyGuard")
    }
  }
}
