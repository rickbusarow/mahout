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

package com.rickbusarow.mahout.publishing

import com.rickbusarow.kgx.dependsOn
import com.rickbusarow.kgx.registerOnce
import com.rickbusarow.mahout.api.DefaultMahoutCheckTask
import com.rickbusarow.mahout.conventions.applyBinaryCompatibility
import com.rickbusarow.mahout.core.check
import com.rickbusarow.mahout.deps.PluginIds
import com.rickbusarow.mahout.dokka.DokkatooConventionPlugin.Companion.dokkaJavadocJar
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugins.signing.Sign
import kotlin.LazyThreadSafetyMode.NONE

/** */
public abstract class MahoutPublishPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    target.plugins.apply(PluginIds.`vanniktech-publish-base`)

    target.rootProject.applyBinaryCompatibility()

    val maven = target.mavenPublishBaseExtension

    maven.publishToMavenCentral(automaticRelease = true)
    maven.signAllPublications()

    val gradlePluginPublish by lazy(NONE) { target.gradlePluginExtension }

    target.mavenPublications.configureEach { publication ->

      if (publication.isPluginMarker()) {

        val plugin = gradlePluginPublish.plugins
          .named(publication.nameWithoutMarker())

        publication.pom.description.set(plugin.map { it.description })
      }
    }

    registerCoordinatesStringsCheckTask(
      target = target,
      mavenPublications = target.mavenPublications
    )
    target.registerSnapshotVersionCheckTask()

    target.tasks.withType(GenerateModuleMetadata::class.java).configureEach { task ->
      task.mustRunAfter(target.tasks.dokkaJavadocJar)
      task.mustRunAfter(target.tasks.named { it == "kotlinSourcesJar" })
    }
    target.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
      it.mustRunAfter(target.tasks.withType(Jar::class.java))
    }
    target.tasks.withType(Sign::class.java).configureEach {
      it.mustRunAfter(target.tasks.withType(Jar::class.java))
    }
  }

  private fun registerCoordinatesStringsCheckTask(
    target: Project,
    mavenPublications: NamedDomainObjectSet<MavenPublication>
  ) {

    val checkTask = target.tasks.registerOnce(
      "checkMavenCoordinatesStrings",
      DefaultMahoutCheckTask::class.java
    ) { task ->
      task.group = "publishing"
      task.description = "checks that the project's maven group and artifact ID are valid for Maven"

      val artifactIds = mavenPublications.map { it.artifactId }

      val groupId = target.group.toString()

      task.onlyIf { artifactIds.isNotEmpty() }

      task.doLast {

        val allowedRegex = "^[A-Za-z0-9_\\-.]+$".toRegex()

        check(groupId.matches(allowedRegex)) {

          val actualString = when {
            groupId.isEmpty() -> "<<empty string>>"
            else -> groupId
          }
          "groupId ($actualString) is not a valid Maven identifier ($allowedRegex)."
        }

        val invalid = artifactIds.filterNot { artifactId -> artifactId.matches(allowedRegex) }

        check(invalid.isEmpty()) {
          invalid.joinToString("\n") { artifactId ->

            val actualString = when {
              artifactId.isEmpty() -> "<<empty string>>"
              else -> artifactId
            }
            "artifactId ($actualString) is not a valid Maven identifier ($allowedRegex)."
          }
        }
      }
    }

    target.tasks.check.dependsOn(checkTask)
  }

  private fun Project.registerSnapshotVersionCheckTask() {
    tasks.registerOnce("checkVersionIsSnapshot", DefaultMahoutCheckTask::class.java) { task ->
      task.group = "publishing"
      task.description = "ensures that the project version has a -SNAPSHOT suffix"
      val versionString = version as String
      task.doLast {
        val expected = "-SNAPSHOT"
        require(versionString.endsWith(expected)) {
          "The project's version name must be suffixed with `$expected` when checked in" +
            " to the main branch, but instead it's `$versionString`."
        }
      }
    }
    tasks.registerOnce("checkVersionIsNotSnapshot", DefaultMahoutCheckTask::class.java) { task ->
      task.group = "publishing"
      task.description = "ensures that the project version does not have a -SNAPSHOT suffix"
      val versionString = version as String
      task.doLast {
        require(!versionString.endsWith("-SNAPSHOT")) {
          "The project's version name cannot have a -SNAPSHOT suffix, but it was $versionString."
        }
      }
    }
  }
}
