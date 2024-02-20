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

package com.rickbusarow.mahout.publishing

import com.rickbusarow.kgx.registerOnce
import com.rickbusarow.mahout.api.DefaultMahoutCheckTask
import com.rickbusarow.mahout.api.DefaultMahoutTask
import com.rickbusarow.mahout.config.gitUrl
import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.conventions.applyBinaryCompatibility
import com.rickbusarow.mahout.config.sshUrl
import com.rickbusarow.mahout.config.url
import com.rickbusarow.mahout.conventions.applyBinaryCompatibility
import com.rickbusarow.mahout.core.VERSION_NAME
import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar.Dokka
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.api.tasks.bundling.Jar
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.plugins.signing.Sign
import org.jetbrains.dokka.gradle.AbstractDokkaLeafTask

@Suppress("UndocumentedPublicClass")
public interface PublishingExtension {

  @Suppress("UndocumentedPublicFunction")
  public fun Project.published(artifactId: String, pomDescription: String) {
    published(
      groupId = mahoutProperties.group.get(),
      artifactId = artifactId,
      pomDescription = pomDescription
    )
  }

  @Suppress("UndocumentedPublicFunction")
  public fun Project.published(groupId: String, artifactId: String, pomDescription: String) {

    plugins.apply("com.vanniktech.maven.publish.base")
    plugins.apply("com.rickbusarow.mahout.dokkatoo")

    configurePublish(
      artifactId = artifactId,
      pomDescription = pomDescription,
      groupId = groupId
    )
  }
}

private fun Project.configurePublish(artifactId: String, pomDescription: String, groupId: String) {

  version = VERSION_NAME
  group = groupId

  @Suppress("UnstableApiUsage")
  extensions.configure(MavenPublishBaseExtension::class.java) { extension ->

    extension.publishToMavenCentral(SonatypeHost.DEFAULT, automaticRelease = true)

    extension.signAllPublications()

    extension.pom { mavenPom ->
      mavenPom.description.set(pomDescription)
      mavenPom.name.set(artifactId)

      mavenPom.url.set(mahoutProperties.repository.github.url)

      mavenPom.licenses { licenseSpec ->
        licenseSpec.license { license ->
          license.name.set("The Apache Software License, Version 2.0")
          license.url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
          license.distribution.set("repo")
        }
      }
      mavenPom.scm { scm ->
        scm.url.set(mahoutProperties.repository.github.url)
        scm.connection.set(mahoutProperties.repository.github.gitUrl)
        scm.developerConnection.set(mahoutProperties.repository.github.sshUrl)
      }
      mavenPom.developers { developerSpec ->
        developerSpec.developer { developer ->

          developer.id.convention(mahoutProperties.publishing.pom.developer.id)
          developer.name.convention(mahoutProperties.publishing.pom.developer.name)
          developer.url.convention(mahoutProperties.publishing.pom.developer.url)
        }
      }
    }

    when {
      // The plugin-publish plugin handles its artifacts
      pluginManager.hasPlugin("com.gradle.plugin-publish") -> {}

      // handle publishing plugins if they're not going to the plugin portal
      pluginManager.hasPlugin("java-gradle-plugin") -> {
        extension.configure(
          GradlePlugin(
            javadocJar = Dokka(taskName = "dokkaHtml"),
            sourcesJar = true
          )
        )
      }

      pluginManager.hasPlugin("com.github.johnrengelman.shadow") -> {
        extension.configure(
          KotlinJvm(javadocJar = Dokka(taskName = "dokkaHtml"), sourcesJar = true)
        )
        applyBinaryCompatibility()
      }

      else -> {
        extension.configure(
          KotlinJvm(javadocJar = Dokka(taskName = "dokkaHtml"), sourcesJar = true)
        )
        applyBinaryCompatibility()
      }
    }

    extensions.configure(PublishingExtension::class.java) { publishingExtension ->
      publishingExtension.publications.withType(
        MavenPublication::class.java
      ).configureEach { publication ->
        publication.artifactId = artifactId
        publication.pom.description.set(pomDescription)
        publication.groupId = groupId
      }
    }
  }

  registerCoordinatesStringsCheckTask(groupId = groupId, artifactId = artifactId)
  registerSnapshotVersionCheckTask()
  configureSkipDokka()

  tasks.withType(PublishToMavenRepository::class.java).configureEach {
    it.notCompatibleWithConfigurationCache("See https://github.com/gradle/gradle/issues/13468")
  }
  tasks.withType(Jar::class.java).configureEach {
    it.notCompatibleWithConfigurationCache("")
  }
  tasks.withType(Sign::class.java).configureEach {
    it.notCompatibleWithConfigurationCache("")
    // skip signing for -SNAPSHOT publishing
    it.onlyIf { !(version as String).endsWith("SNAPSHOT") }
  }
}

private fun Project.registerCoordinatesStringsCheckTask(groupId: String, artifactId: String) {

  val checkTask = tasks.registerOnce(
    "checkMavenCoordinatesStrings",
    DefaultMahoutCheckTask::class.java
  ) { task ->
    task.group = "publishing"
    task.description = "checks that the project's maven group and artifact ID are valid for Maven"

    task.doLast {

      val allowedRegex = "^[A-Za-z0-9_\\-.]+$".toRegex()

      check(groupId.matches(allowedRegex)) {

        val actualString = when {
          groupId.isEmpty() -> "<<empty string>>"
          else -> groupId
        }
        "groupId ($actualString) is not a valid Maven identifier ($allowedRegex)."
      }

      check(artifactId.matches(allowedRegex)) {

        val actualString = when {
          artifactId.isEmpty() -> "<<empty string>>"
          else -> artifactId
        }
        "artifactId ($actualString) is not a valid Maven identifier ($allowedRegex)."
      }
    }
  }

  tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) { task ->
    task.dependsOn(checkTask)
  }
}

private fun Project.registerSnapshotVersionCheckTask() {
  tasks.registerOnce(
    "checkVersionIsSnapshot",
    DefaultMahoutCheckTask::class.java
  ) { task ->
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

/**
 * Integration tests require `publishToMavenLocal`, but they definitely don't need
 * Dokka output, and generating kdoc for everything takes forever -- especially
 * on a GitHub Actions server. So for integration tests, skip Dokka tasks.
 */
private fun Project.configureSkipDokka() {

  if (tasks.names.contains("setSkipDokka")) {
    return
  }

  var skipDokka = false
  val setSkipDokka = tasks.register(
    "setSkipDokka",
    DefaultMahoutTask::class.java
  ) { task ->

    task.group = "publishing"
    task.description = "sets `skipDokka` to true before `publishToMavenLocal` is evaluated."

    task.doFirst { skipDokka = true }
    task.onlyIf { true }
  }

  tasks.register("publishToMavenLocalNoDokka", DefaultMahoutTask::class.java) {

    it.group = "publishing"
    it.description = "Delegates to `publishToMavenLocal`, " +
      "but skips Dokka generation and does not include a javadoc .jar."

    it.doFirst { skipDokka = true }
    it.dependsOn(setSkipDokka)
    it.onlyIf { true }
    it.dependsOn("publishToMavenLocal")
  }

  tasks.matching { it.name == "javaDocReleaseGeneration" }.configureEach {
    it.onlyIf { !skipDokka }
  }
  tasks.withType(AbstractDokkaLeafTask::class.java).configureEach {
    it.onlyIf { !skipDokka }
  }

  tasks.named("publishToMavenLocal") {
    it.mustRunAfter(setSkipDokka)
  }
}
