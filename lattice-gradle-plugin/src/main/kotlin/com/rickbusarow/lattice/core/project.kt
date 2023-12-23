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

import com.rickbusarow.kgx.isPartOfRootBuild
import com.rickbusarow.kgx.parents
import org.gradle.api.Project

internal fun Project.addTasksToStartParameter(taskNames: Iterable<String>) {

  if (isPartOfRootBuild) {
    /* Root of composite build: We can just add the task name */
    gradle.startParameter.setTaskNames(
      gradle.startParameter.taskNames.toSet() + taskNames
    )
  } else {
    /* This is an included build. Referencing the task path explicitly */
    val rootBuild = gradle.parents().last()

    // val buildId = (project as ProjectInternal).identityPath
    @Suppress("UnstableApiUsage")
    val buildId = project.buildTreePath
    val absoluteTaskPaths = taskNames.map { "$buildId${it.prefixIfNot(":")}" }
    rootBuild.startParameter.setTaskNames(
      rootBuild.startParameter.taskNames.toSet() + absoluteTaskPaths
    )
  }
}
