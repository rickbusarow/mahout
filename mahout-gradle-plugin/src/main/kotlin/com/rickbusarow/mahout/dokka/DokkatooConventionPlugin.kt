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

package com.rickbusarow.mahout.dokka

import com.rickbusarow.kgx.dependsOn
import com.rickbusarow.kgx.extras
import com.rickbusarow.kgx.getOrPut
import com.rickbusarow.kgx.isRootProject
import com.rickbusarow.kgx.projectDependency
import com.rickbusarow.ktlint.KtLintTask
import com.rickbusarow.mahout.api.DefaultMahoutCheckTask
import com.rickbusarow.mahout.api.DefaultMahoutJavadocJarTask
import com.rickbusarow.mahout.api.MahoutFixTask
import com.rickbusarow.mahout.config.JavaVersion.Companion.major
import com.rickbusarow.mahout.conventions.DokkaVersionArchivePlugin.Companion.dokkaArchiveBuildDir
import com.rickbusarow.mahout.conventions.HasGitHubSubExtension
import com.rickbusarow.mahout.conventions.HasJavaSubExtension
import com.rickbusarow.mahout.conventions.HasKotlinSubExtension
import com.rickbusarow.mahout.core.check
import com.rickbusarow.mahout.core.stdlib.SEMVER_REGEX
import com.rickbusarow.mahout.deps.Libs
import com.rickbusarow.mahout.mahoutExtension
import dev.adamko.dokkatoo.DokkatooExtension
import dev.adamko.dokkatoo.dokka.plugins.DokkaVersioningPluginParameters
import dev.adamko.dokkatoo.tasks.DokkatooGenerateModuleTask
import dev.adamko.dokkatoo.tasks.DokkatooGeneratePublicationTask
import dev.adamko.dokkatoo.tasks.DokkatooGenerateTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

/** */
public abstract class DokkatooConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {

    target.pluginManager.apply(dev.adamko.dokkatoo.DokkatooPlugin::class.java)

    val mahoutExtension = target.mahoutExtension

    val dokkaSubExtension = (mahoutExtension as HasDokkaSubExtension).dokka
    val gitHubSubExtension = (mahoutExtension as HasGitHubSubExtension).github
    val kotlinSubExtension = (mahoutExtension as HasKotlinSubExtension).kotlin
    val javaSubExtension = (mahoutExtension as HasJavaSubExtension).java

    target.extensions.configure(DokkatooExtension::class.java) { dokkatoo ->

      dokkatoo.versions.jetbrainsDokka.set(dokkaSubExtension.dokkaVersion)

      dokkatoo.moduleVersion.set(mahoutExtension.group)

      val fullModuleName = target.path.removePrefix(":")
      dokkatoo.moduleName.set(fullModuleName)

      dokkatoo.dokkatooSourceSets.configureEach { sourceSet ->
        sourceSet.documentedVisibilities(
          dev.adamko.dokkatoo.dokka.parameters.VisibilityModifier.PRIVATE,
          dev.adamko.dokkatoo.dokka.parameters.VisibilityModifier.INTERNAL,
          dev.adamko.dokkatoo.dokka.parameters.VisibilityModifier.PROTECTED,
          dev.adamko.dokkatoo.dokka.parameters.VisibilityModifier.PACKAGE,
          dev.adamko.dokkatoo.dokka.parameters.VisibilityModifier.PUBLIC
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

        sourceSet.sourceLink { sourceLinkBuilder ->

          sourceLinkBuilder.localDirectory.set(target.file("src/${sourceSet.name}"))

          val modulePath = target.path.replace(":", "/")
            .replaceFirst("/", "")

          // URL showing where the source code can be accessed through the web browser
          sourceLinkBuilder.remoteUrl.set(
            gitHubSubExtension.url
              .map(::URI)
              .zip(gitHubSubExtension.defaultBranch) { uri, branch ->

                uri.resolve("blob/$branch/$modulePath/src/${sourceSet.name}")
              }
          )
          // Suffix which is used to append the line number to the URL. Use #L for GitHub
          sourceLinkBuilder.remoteLineSuffix.set("#L")
        }
      }

      target.tasks.withType(DokkatooGenerateTask::class.java).configureEach { task ->

        task.workerIsolation.set(dokkatoo.ClassLoaderIsolation())

        // Dokka uses their outputs but doesn't explicitly depend upon them.
        task.mustRunAfter(target.tasks.withType(KotlinCompile::class.java))
        task.mustRunAfter(target.tasks.withType(MahoutFixTask::class.java))
        task.mustRunAfter(target.tasks.withType(KtLintTask::class.java))
      }

      addJavadocHooks(target)
      addPublishingHooks(target)

      if (target.isRootProject()) {

        target.configurations.getByName("dokkatoo").dependencies.let { deps ->

          val allProjects = target.subprojects
            .asSequence()
            .filter { it.buildFile.exists() }
            .filter { it.name != "kase-overload-generator" }
            .map { sub -> target.projectDependency(sub.path) }

          deps.addAll(allProjects)
        }

        val pluginConfig = "dokkatooPluginHtml"

        target.dependencies.add(pluginConfig, Libs.`dokka-versioning`)

        val dokkaArchiveBuildDir = target.dokkaArchiveBuildDir()

        dokkatoo.pluginsConfiguration
          .withType(DokkaVersioningPluginParameters::class.java)
          .configureEach { versioning ->

            versioning.version.set(mahoutExtension.versionName)
            if (dokkaArchiveBuildDir.get().asFile.exists()) {
              versioning.olderVersionsDir.set(dokkaArchiveBuildDir)
            }
            versioning.renderVersionsNavigationOnAllPages.set(true)
          }

        dokkatoo.dokkatooPublications.configureEach {
          it.suppressObviousFunctions.set(true)
        }
      }
    }
  }

  private fun addJavadocHooks(target: Project) {
    // The `org.gradle.plugin-publish` plugin
    // adds the `javadocJar` output as the `javadoc` artifact,
    // but it doesn't include the Dokka output by default.
    target.tasks.withType(Jar::class.java)
      .named { it == "javadocJar" }
      .configureEach { it.from(target.dokkatooGenerateModuleHtmlTask()) }

    target.tasks.register("dokkaJavadocJar", DefaultMahoutJavadocJarTask::class.java) {

      val skipDokka = target.extras.getOrPut("skipDokka") { false }

      it.archiveClassifier.set("javadoc")

      if (!skipDokka) {
        val dokkaTask = target.dokkatooGenerateModuleHtmlTask()

        // it.dependsOn(dokkaTask)
        it.from(dokkaTask)
      }
    }
  }

  private fun addPublishingHooks(target: Project) {
    target.plugins.withType(MavenPublishPlugin::class.java).configureEach {

      val javadocTasks = target.tasks
        .withType(DefaultMahoutJavadocJarTask::class.java)

      val checkJavadocJarIsNotVersioned = target.tasks
        .register("checkJavadocJarIsNotVersioned", DefaultMahoutCheckTask::class.java) { task ->

          task.description =
            "Ensures that generated javadoc.jar artifacts don't include old Dokka versions"
          task.group = "dokka versioning"

          task.inputs.files(javadocTasks)

          val zipTrees = javadocTasks.map { target.zipTree(it.archiveFile) }

          task.doLast {

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

    internal fun Project.dokkatooGenerateModuleHtmlTask(): TaskProvider<DokkatooGenerateModuleTask> {
      return tasks.named("dokkatooGenerateModuleHtml", DokkatooGenerateModuleTask::class.java)
    }

    internal fun Project.dokkatooGeneratePublicationHtmlTask(): TaskProvider<DokkatooGeneratePublicationTask> {
      return tasks.named(
        "dokkatooGeneratePublicationHtml",
        DokkatooGeneratePublicationTask::class.java
      )
    }
  }
}
