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

package com.rickbusarow.lattice.composite

import com.rickbusarow.kgx.checkProjectIsRoot
import com.rickbusarow.kgx.internal.InternalGradleApiAccess
import com.rickbusarow.kgx.internal.allIncludedProjects
import com.rickbusarow.lattice.RootExtension
import com.rickbusarow.lattice.composite.CompositeSubExtension.RequestedTask
import com.rickbusarow.lattice.composite.CompositeSubExtension.ResolvedTask
import com.rickbusarow.lattice.core.namedOrNull
import com.rickbusarow.lattice.stdlib.splitInclusive
import modulecheck.utils.mapToSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.DefaultTaskExecutionRequest

/** This plugin finds specified tasks in included builds and */
@InternalGradleApiAccess
public abstract class CompositePlugin : Plugin<Project> {

  @OptIn(InternalGradleApiAccess::class)
  override fun apply(target: Project) {

    target.checkProjectIsRoot()
    require(target.gradle.includedBuilds.isNotEmpty()) {
      "Only apply the 'composite' plugin to a root project with included builds.  " +
        "This project has no included builds, " +
        "so the plugin would just waste time searching the task graph."
    }

    val extension = target.extensions.getByType(RootExtension::class.java)

    target.gradle.projectsEvaluated { gradle ->

      val oldRequests = gradle.startParameter.taskRequests

      val newRequests = oldRequests.map { request ->

        val originalSplit = request.args
          .splitInclusive { !it.startsWith('-') }

        val taskPaths = originalSplit.mapToSet { it.first() }

        val includedProjects = gradle.allIncludedProjects()

        val newSplit = originalSplit.flatMap { taskWithArgs ->

          val taskName = taskWithArgs.first()

          if (taskName.startsWith(':')) {
            // qualified task names aren't propagated to included builds
            return@flatMap listOf(taskWithArgs)
          }

          val resolvedInRootBuild = target.tasks.namedOrNull(taskName)

          when {
            // there's no point in just repeating the same help text
            taskName == "help" -> return@flatMap listOf(taskWithArgs)
            // don't include tasks that aren't requested
            !extension.composite.includeRequested.isSatisfiedBy(
              RequestedTask(
                name = taskName,
                typeOrNull = resolvedInRootBuild?.publicType
              )
            ) -> return@flatMap listOf(taskWithArgs)
          }

          val inRoot = resolvedInRootBuild != null

          val included = includedProjects.mapNotNull { includedProject ->

            val includedPath = "${includedProject.identityPath}:$taskName"

            // Don't include tasks that are already in the task graph
            if (taskPaths.contains(includedPath)) return@mapNotNull null

            val resolved = includedProject.tasks.namedOrNull(taskName)
              ?: return@mapNotNull null

            val r = ResolvedTask(
              buildPath = includedProject.rootProject.name,
              taskPath = includedPath,
              taskName = taskName,
              type = resolved.publicType
            )

            if (extension.composite.includeCompositeTasks.isSatisfiedBy(r)) {
              target.logger.quiet("The task $taskName will delegate to $includedPath")

              buildList<String> {
                add(includedPath)
                addAll(taskWithArgs.subList(1, taskWithArgs.size))
              }
            } else {
              null
            }
          }

          buildList {
            if (inRoot || included.isEmpty()) {
              add(taskWithArgs)
            }
            addAll(included)
          }
        }

        DefaultTaskExecutionRequest.of(newSplit.flatten(), request.projectPath, request.rootDir)
      }

      gradle.startParameter.setTaskRequests(newRequests)
    }
  }
}
