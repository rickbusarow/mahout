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

package com.rickbusarow.mahout.conventions

import com.rickbusarow.kgx.applyOnce
import com.rickbusarow.kgx.dependsOn
import com.rickbusarow.kgx.extras
import com.rickbusarow.kgx.javaExtension
import com.rickbusarow.kgx.kotlinJvmExtensionSafe
import com.rickbusarow.kgx.names.DomainObjectName
import com.rickbusarow.kgx.names.SourceSetName
import com.rickbusarow.kgx.names.SourceSetName.Companion.addPrefix
import com.rickbusarow.kgx.names.SourceSetName.Companion.isMain
import com.rickbusarow.kgx.project
import com.rickbusarow.kgx.withJavaGradlePluginPlugin
import com.rickbusarow.mahout.api.MahoutTask
import com.rickbusarow.mahout.core.stdlib.capitalize
import com.rickbusarow.mahout.deps.Versions
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.testing.base.TestingExtension

/** */
public interface GradleTestsExtension {

  /**
   * Adds a `gradleTest` source set to the project.
   *
   * @see GradleTestsPlugin
   */
  @Suppress("UndocumentedPublicFunction")
  public fun Project.gradleTests() {
    plugins.apply(GradleTestsPlugin::class.java)
  }
}

/**
 * This plugin will:
 * - add a `gradleTest` source set to the project.
 * - declare that source set as a "test" source set in the IDE.
 * - register a `gradleTest` task that runs the tests in the `gradleTest` source set.
 * - make the `check` task depend upon the `gradleTest` task.
 * - make the `gradleTest` task depend upon the `publishToBuildM2` task.
 */
public abstract class GradleTestsPlugin : Plugin<Project> {

  @Suppress("UnstableApiUsage")
  override fun apply(target: Project) {

    target.plugins.applyOnce("jvm-test-suite")

    val suiteName = TestSuiteName("gradleTest")
    val repoName = "buildM2"

    val testingExtension = target.extensions.getByType(TestingExtension::class.java)

    val suite = testingExtension.suites
      .register(suiteName.value, JvmTestSuite::class.java) { suite ->
        suite.useJUnitJupiter(Versions.jUnit5)

        suite.dependencies {
          it.implementation.add(target.dependencies.project(target.path))
          it.implementation.add(target.dependencies.gradleTestKit())
        }
      }

    // Tells the `java-gradle-plugin` plugin to inject its TestKit logic
    // into the `gradleTest` source set.
    target.gradlePluginExtensionSafe { extension ->
      extension.testSourceSets(target.javaSourceSet(suiteName.value))
    }

    target.kotlinJvmExtensionSafe { kotlinExtension ->

      val compilations = kotlinExtension.target.compilations

      compilations.named(suiteName.value) {
        it.associateWith(compilations.getByName("main"))
      }
    }

    val realPublishTask = "publishAllPublicationsTo${repoName.capitalize()}Repository"

    target.rootProject.allprojects { anyProject ->

      anyProject.plugins.withType(PublishingPlugin::class.java).configureEach {
        setUpPublishToBuildM2(anyProject, repoName)
      }

      suite.configure { st ->
        st.targets.configureEach { suiteTarget ->
          suiteTarget.testTask.configure { testTask ->
            testTask.dependsOn(anyProject.tasks.named { it == realPublishTask })
          }
        }
      }
    }

    // Make `check` depend upon `gradleTest`
    target.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).dependsOn(suiteName.value)
  }

  /**
   * Registers this [target]'s version of the `publishToBuildM2`
   * task and adds it as a dependency to the root project's version.
   */
  private fun setUpPublishToBuildM2(target: Project, repoName: String) {

    val buildM2Dir = target.rootProject.layout.buildDirectory.dir("gradle-test-m2")

    target.gradlePublishingExtension.repositories { repositories ->
      repositories.mavenLocal {
        it.name = repoName
        it.setUrl(buildM2Dir)
      }
    }
    target.tasks.register("publishToBuildM2") {
      it.group = "Publishing"
      it.dependsOn("publishAllPublicationsToBuildM2Repository")
    }
    target.tasks.register("publishToBuildM2NoDokka") {
      it.group = "Publishing"
      target.extras.set("skipDokka", true)
      it.dependsOn("publishAllPublicationsToBuildM2Repository")
    }
  }

  private fun Project.javaSourceSet(name: String): SourceSet {
    return javaExtension.sourceSets.getByName(name)
  }
}

/** */
public interface MahoutPublishTask : MahoutTask

/** */
public abstract class DefaultMahoutPublishTask : DefaultTask(), MahoutPublishTask

internal fun MavenPublication.isPluginMarker(): Boolean = name.endsWith("PluginMarkerMaven")
internal fun MavenPublication.nameWithoutMarker(): String = name.removeSuffix("PluginMarkerMaven")
internal fun Publication.isPluginMarker(): Boolean =
  (this as? MavenPublication)?.isPluginMarker() ?: false

internal val Project.mavenPublishBaseExtension: MavenPublishBaseExtension
  get() = extensions.getByType(MavenPublishBaseExtension::class.java)

internal val Project.gradlePublishingExtension: PublishingExtension
  get() = extensions.getByType(PublishingExtension::class.java)

internal val Project.gradlePluginExtension: GradlePluginDevelopmentExtension
  get() = extensions.getByType(GradlePluginDevelopmentExtension::class.java)

internal fun Project.gradlePluginExtensionSafe(action: Action<GradlePluginDevelopmentExtension>) {
  plugins.withJavaGradlePluginPlugin {
    action.execute(gradlePluginExtension)
  }
}

internal val Project.mavenPublications: NamedDomainObjectSet<MavenPublication>
  get() = gradlePublishingExtension.publications.withType(MavenPublication::class.java)

@JvmInline
internal value class TestSuiteName(override val value: String) : DomainObjectName<Publication> {

  companion object {

    fun forSourceSetName(baseName: String, sourceSetName: String): TestSuiteName {
      return forSourceSetName(baseName, SourceSetName(sourceSetName))
    }

    fun forSourceSetName(baseName: String, sourceSetName: SourceSetName): TestSuiteName {
      return if (sourceSetName.isMain()) {
        TestSuiteName(baseName)
      } else {
        TestSuiteName(sourceSetName.addPrefix(baseName))
      }
    }

    fun String.asTestSuiteName(): TestSuiteName = TestSuiteName(this)
  }
}
