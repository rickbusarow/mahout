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

package com.rickbusarow.lattice

import com.rickbusarow.lattice.conventions.AbstractHasSubExtension
import com.rickbusarow.lattice.conventions.AbstractSubExtension
import com.rickbusarow.lattice.core.SubExtension
import com.rickbusarow.lattice.core.SubExtensionInternal
import com.rickbusarow.lattice.core.addTasksToStartParameter
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

public interface HasTasksSubExtension : java.io.Serializable {
  public val tasks: TasksSubExtension
  public fun tasks(action: Action<in TasksSubExtension>) {
    action.execute(tasks)
  }
}

internal abstract class DefaultHasTasksSubExtension @Inject constructor(
  override val objects: ObjectFactory
) : AbstractHasSubExtension(), HasTasksSubExtension {
  override val tasks: TasksSubExtension by subExtension(DefaultTasksSubExtension::class)
}

public interface TasksSubExtension : SubExtension<TasksSubExtension> {

  /**
   * Eagerly adds the given tasks to the IDE sync task. This is useful for tasks that
   * generate code that is needed by the IDE, such as the `generateBuildConfig` task.
   */
  public fun addTasksToIdeSync(vararg taskNames: String)
}

public abstract class DefaultTasksSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : AbstractSubExtension(target, objects),
  TasksSubExtension,
  SubExtensionInternal {

  override fun addTasksToIdeSync(vararg taskNames: String) {
    target.addTasksToStartParameter(taskNames.toList())
  }
}
