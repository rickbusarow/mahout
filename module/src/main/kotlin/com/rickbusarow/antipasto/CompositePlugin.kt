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

package com.rickbusarow.antipasto

import com.rickbusarow.antipasto.core.splitInclusive
import com.rickbusarow.kgx.checkProjectIsRoot
import com.rickbusarow.kgx.internal.allIncludedProjects
import modulecheck.utils.mapToSet
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.internal.DefaultTaskExecutionRequest
import javax.inject.Inject

@Suppress("UndocumentedPublicClass")
public interface CompositeHandler : java.io.Serializable {
  /**
   */
  public val composite: CompositeTaskSpec

  /**
   */
  public fun composite(action: Action<CompositeTaskSpec>)
}

@Suppress("UndocumentedPublicClass")
public abstract class DefaultCompositeHandler @Inject constructor(
  private val target: Project,
  private val objects: ObjectFactory
) : CompositeHandler {

  override val composite: CompositeTaskSpec =
    objects.newInstance(CompositeTaskSpec::class.java)

  override fun composite(action: Action<CompositeTaskSpec>) {
    action.execute(composite)
  }
}

@Suppress("UndocumentedPublicClass")
public open class CompositeTaskSpec @Inject constructor(
  private val target: Project,
  private val objects: ObjectFactory
)

/** This plugin finds specified tasks in included builds and */
public abstract class CompositePlugin : Plugin<Project> {

  override fun apply(target: Project) {

    target.checkProjectIsRoot()
    require(target.gradle.includedBuilds.isNotEmpty()) {
      "Only apply the 'composite' plugin to a root project with included builds.  " +
        "This project has no included builds, " +
        "so the plugin would just waste time searching the task graph."
    }

    // val extension = target.extensions.getByType(RootExtension::class.java)

    target.gradle.projectsEvaluated { gradle ->

      val oldRequests = gradle.startParameter.taskRequests

      val newRequests = oldRequests.map { request ->

        val originalSplit = request.args
          .splitInclusive { !it.startsWith('-') }

        val taskPaths = originalSplit.mapToSet { it.first() }

        val includedProject = gradle.allIncludedProjects()

        val newSplit = originalSplit.flatMap { ta ->

          val tn = ta.first()

          if (tn.startsWith(':')) {
            // qualified task names aren't propagated
            return@flatMap listOf(ta)
          }

          val inRoot = target.taskWillResolveInAny(tn)

          val included = includedProject.mapNotNull { includedProject ->

            val includedPath = "${includedProject.identityPath}:$tn"

            if (!taskPaths.contains(includedPath) && includedProject.taskWillResolve(tn)) {
              println("included project ${includedProject.identityPath} will resolve $includedPath")

              buildList<String> {
                add(includedPath)
                addAll(ta.subList(1, ta.size))
              }
            } else {
              null
            }
          }

          buildList {
            if (inRoot || included.isEmpty()) {
              add(ta)
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
