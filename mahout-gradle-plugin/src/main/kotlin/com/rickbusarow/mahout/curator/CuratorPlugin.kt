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

package com.rickbusarow.mahout.curator

import com.rickbusarow.kgx.checkProjectIsRoot
import com.rickbusarow.kgx.dependOn
import com.rickbusarow.kgx.dependsOn
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.language.base.plugins.LifecycleBasePlugin

/** */
public abstract class CuratorPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    target.checkProjectIsRoot()

    val artifactsDump = target.tasks.register("artifactsDump", CuratorDumpTask::class.java)
    val artifactsCheck = target.tasks.register("artifactsCheck", CuratorCheckTask::class.java)

    target.tasks.register("curatorDump") {
      it.description = "alias for `artifactsDump`"
      it.dependsOn(artifactsDump)
    }
    target.tasks.register("curatorCheck") {
      it.description = "alias for `artifactsCheck`"
      it.dependsOn(artifactsCheck)
    }

    target.plugins.apply(LifecycleBasePlugin::class.java)

    target.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).dependsOn(artifactsCheck)

    target.allprojects {
      it.tasks.withType(AbstractPublishToMaven::class.java).dependOn(artifactsCheck)
    }
  }
}
