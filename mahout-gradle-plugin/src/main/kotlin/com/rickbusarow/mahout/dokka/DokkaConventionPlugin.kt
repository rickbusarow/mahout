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

package com.rickbusarow.mahout.dokka

import com.rickbusarow.kgx.dependsOn
import com.rickbusarow.kgx.extras
import com.rickbusarow.kgx.getOrPut
import com.rickbusarow.kgx.isRootProject
import com.rickbusarow.kgx.projectDependency
import com.rickbusarow.ktlint.internal.mapToSet
import com.rickbusarow.mahout.api.DefaultMahoutCheckTask
import com.rickbusarow.mahout.api.DefaultMahoutJavadocJarTask
import com.rickbusarow.mahout.config.JavaVersion.Companion.major
import com.rickbusarow.mahout.conventions.DokkaVersionArchivePlugin.Companion.dokkaArchiveBuildDir
import com.rickbusarow.mahout.conventions.DokkaVersionArchivePlugin.Companion.dokkaArchiveDir
import com.rickbusarow.mahout.conventions.HasGitHubSubExtension
import com.rickbusarow.mahout.conventions.HasJavaSubExtension
import com.rickbusarow.mahout.conventions.HasKotlinSubExtension
import com.rickbusarow.mahout.core.check
import com.rickbusarow.mahout.core.stdlib.SEMVER_REGEX
import com.rickbusarow.mahout.deps.Libs
import com.rickbusarow.mahout.deps.PluginIds
import com.rickbusarow.mahout.mahoutExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.dokka.gradle.engine.plugins.DokkaVersioningPluginParameters
import org.jetbrains.dokka.gradle.tasks.DokkaGenerateModuleTask
import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.net.URI

/** */
public abstract class DokkaConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {

    target.extraProperties.apply {
      set("org.jetbrains.dokka.experimental.gradle.pluginMode", "V2Enabled")
      set("org.jetbrains.dokka.experimental.gradle.pluginMode.noWarn", "true")
    }

    target.plugins.apply(PluginIds.`jetbrains-dokka`)

    val mahoutExtension = target.mahoutExtension

    val gitHubSubExtension = (mahoutExtension as HasGitHubSubExtension).github
    val kotlinSubExtension = (mahoutExtension as HasKotlinSubExtension).kotlin
    val javaSubExtension = (mahoutExtension as HasJavaSubExtension).java

    val dokka = target.extensions.getByType(DokkaExtension::class.java)

    dokka.moduleVersion.set(mahoutExtension.versionName)

    val fullModuleName = target.path.removePrefix(":")
    dokka.moduleName.set(fullModuleName)

    dokka.dokkaSourceSets.configureEach { sourceSet ->

      sourceSet.documentedVisibilities(
        VisibilityModifier.Private,
        VisibilityModifier.Internal,
        VisibilityModifier.Protected,
        VisibilityModifier.Package,
        VisibilityModifier.Public
      )

      sourceSet.languageVersion.set(kotlinSubExtension.apiLevel)
      sourceSet.jdkVersion.set(javaSubExtension.jvmTarget.major)

      // include all project sources when resolving kdoc samples
      sourceSet.samples.setFrom(target.fileTree(target.file("src")))

      if (!target.isRootProject()) {
        val readmeFile = target.projectDir.resolve("README.md")
        if (readmeFile.exists()) {
          sourceSet.includes.from(readmeFile)
        }
      }

      val modulePath = target.path.replace(":", "/")
        .replaceFirst("/", "")

      val remoteUrl = gitHubSubExtension.url
        .map(::URI)
        .zip(gitHubSubExtension.defaultBranch) { uri, branch ->
          uri.resolve("blob/$branch/$modulePath/src/${sourceSet.name}")
        }

      if (remoteUrl.isPresent) {

        sourceSet.sourceLink { sourceLinkBuilder ->

          sourceLinkBuilder.localDirectory.set(target.file("src/${sourceSet.name}"))

          // URL showing where the source code can be accessed through the web browser
          sourceLinkBuilder.remoteUrl.set(remoteUrl)
          // Suffix which is used to append the line number to the URL. Use #L for GitHub
          sourceLinkBuilder.remoteLineSuffix.set("#L")
        }
      }
    }

    addJavadocHooks(target)
    addPublishingHooks(target)

    if (target.isRootProject()) {

      val config = target.configurations.getByName("dokka")

      config.dependencies.addAllLater(
        target.provider {
          target.subprojects
            .filter { sub -> sub.subprojects.isEmpty() }
            .map { sub -> target.projectDependency(sub.path) }
        }
      )

      target.dependencies.add("dokkaPlugin", Libs.`dokka-versioning`)

      val dokkaArchiveDir = target.dokkaArchiveDir()
      val dokkaArchiveBuildDir = target.dokkaArchiveBuildDir()

      dokka.pluginsConfiguration.withType(DokkaVersioningPluginParameters::class.java)
        .configureEach { versioning ->
          versioning.version.set(mahoutExtension.versionName)

          if (dokkaArchiveDir.exists()) {
            versioning.olderVersionsDir.set(dokkaArchiveBuildDir)
          }

          versioning.renderVersionsNavigationOnAllPages.set(true)
        }
    }
  }

  private fun addJavadocHooks(target: Project) {
    // The `org.gradle.plugin-publish` plugin
    // adds the `javadocJar` output as the `javadoc` artifact,
    // but it doesn't include the Dokka output by default.
    target.tasks.withType(Jar::class.java)
      .named { it == "javadocJar" }
      .configureEach { it.from(target.dokkaGenerateModuleHtmlTask()) }

    target.tasks.register("dokkaJavadocJar", DefaultMahoutJavadocJarTask::class.java) { task ->

      val skipDokka = task.extras.getOrPut("skipDokka") { false }

      task.archiveClassifier.set("javadoc")

      if (!skipDokka) {
        val dokkaTask = target.dokkaGenerateModuleHtmlTask()

        // task.dependsOn(dokkaTask)
        task.from(dokkaTask)
      }
    }
  }

  private fun addPublishingHooks(target: Project) {
    target.plugins.withType(MavenPublishPlugin::class.java).configureEach { _ ->

      val mahoutJavadocJarTasks = target.tasks
        .withType(DefaultMahoutJavadocJarTask::class.java)

      val javadocJarTasks = target.tasks
        .withType(Jar::class.java)
        .named { it.contains("javadoc", ignoreCase = true) }

      val checkJavadocJarIsNotVersioned = target.tasks
        .register("checkJavadocJarIsNotVersioned", DefaultMahoutCheckTask::class.java) { task ->

          task.description =
            "Ensures that generated javadoc.jar artifacts don't include old Dokka versions"
          task.group = "dokka versioning"

          task.inputs.files(mahoutJavadocJarTasks)
          task.inputs.files(javadocJarTasks)

          val zipTrees = mahoutJavadocJarTasks.plus(javadocJarTasks)
            .mapToSet { it.archiveFile }
            .map { target.zipTree(it) }

          task.doLast { _ ->

            val jsonReg = """older/($SEMVER_REGEX)/version\.json""".toRegex()

            val versions = zipTrees.flatMap { tree ->
              tree
                .filter { it.path.startsWith("older/") }
                .filter { it.isFile }
                .mapNotNull { jsonReg.find(it.path)?.groupValues?.get(1) }
            }

            if (versions.isNotEmpty()) {
              throw GradleException("Found old Dokka versions in javadoc.jar: $versions")
            }
          }
        }

      target.tasks.check.dependsOn(checkJavadocJarIsNotVersioned)
    }
  }

  internal companion object {
    internal val TaskContainer.dokkaJavadocJar: TaskProvider<DefaultMahoutJavadocJarTask>
      get() = named("dokkaJavadocJar", DefaultMahoutJavadocJarTask::class.java)

    internal fun Project.dokkaGenerateModuleHtmlTask(): TaskProvider<DokkaGenerateModuleTask> {
      return tasks.named("dokkaGenerateModuleHtml", DokkaGenerateModuleTask::class.java)
    }

    internal fun Project.dokkaGeneratePublicationHtmlTask(): TaskProvider<DokkaGeneratePublicationTask> {
      return tasks.named("dokkaGeneratePublicationHtml", DokkaGeneratePublicationTask::class.java)
    }
  }
}
