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

import com.rickbusarow.kgx.applyOnce
import com.rickbusarow.kgx.dependsOn
import com.rickbusarow.kgx.isRootProject
import com.rickbusarow.mahout.api.MahoutTask
import com.rickbusarow.mahout.core.clean
import com.rickbusarow.mahout.core.stdlib.isOrphanedBuildOrGradleDir
import com.rickbusarow.mahout.core.stdlib.isOrphanedGradleProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import java.io.File

/** */
public abstract class CleanPlugin : Plugin<Project> {
  override fun apply(target: Project) {

    target.plugins.applyOnce("base")

    val deleteEmptyDirs = target.tasks
      .register("deleteEmptyDirs", DeleteEmptyDirsTask::class.java) { task ->
        task.description = "Delete all empty directories within a project."

        val subprojectDirs = target.subprojects.map { it.projectDir.path }

        val empties = target.provider {
          target.projectDir.walkBottomUp()
            .filter { it.isDirectory }
            .filterNot { dir -> subprojectDirs.any { dir.path.startsWith(it) } }
            .filterNot { it.path.contains(".gradle") }
            .filter { it.hasOnlyEmptySubdirectories() }
            .toList()
        }
        task.delete(empties)
      }

    target.tasks.clean.dependsOn(deleteEmptyDirs)

    target.tasks.register("cleanGradle", CleanGradleTask::class.java) { task ->
      task.delete(".gradle")
    }

    if (target.isRootProject()) {
      val deleteOrphanedProjectDirs = target.tasks
        .register("deleteOrphanedProjectDirs", DeleteOrphanedProjectDirsTask::class.java) { task ->

          task.description = buildString {
            append("Delete any 'build' or `.gradle` directory or `gradle.properties` file ")
            append("without an associated Gradle project.")
          }

          task.delete(
            target.fileTree(target.projectDir) { tree ->
              tree.exclude("**/.git/")
                .exclude("${target.rootDir}/website/node_modules")
                .include { it.file.isOrphanedBuildOrGradleDir() || it.file.isOrphanedGradleProperties() }
            }
          )
        }

      deleteEmptyDirs.dependsOn(deleteOrphanedProjectDirs)
    }
  }
}

/** */
public abstract class MahoutCleanTask : Delete(), MahoutTask

/** */
public abstract class CleanGradleTask : Delete(), MahoutTask

/** */
public abstract class DeleteEmptyDirsTask : MahoutCleanTask(), MahoutTask

/** */
public abstract class DeleteOrphanedProjectDirsTask : MahoutCleanTask(), MahoutTask

internal fun File.hasOnlyEmptySubdirectories(): Boolean {
  return !isFile && walkBottomUp().none { it.isFile }
}
