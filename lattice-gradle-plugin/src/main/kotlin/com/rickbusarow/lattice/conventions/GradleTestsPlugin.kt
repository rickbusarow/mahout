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

package com.rickbusarow.lattice.conventions

import com.rickbusarow.kgx.applyOnce
import com.rickbusarow.kgx.dependsOn
import com.rickbusarow.kgx.extras
import com.rickbusarow.kgx.isRootProject
import com.rickbusarow.kgx.javaExtension
import com.rickbusarow.kgx.registerOnce
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugins.ide.idea.model.IdeaModel

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
 * - make the `gradleTest` task depend upon the `publishToBuildM2` task..
 */
public abstract class GradleTestsPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.plugins.applyOnce("idea")

    val gradleTestSourceSet = target.javaExtension
      .sourceSets
      .register(GRADLE_TEST) { ss ->
        // Tells the `java-gradle-plugin` plugin to inject its TestKit logic
        // into the `gradleTest` source set.
        target.plugins.withId("java-gradle-plugin") {
          target.gradlePluginExtension.testSourceSets(ss)
        }

        val main = target.javaSourceSet(SourceSet.MAIN_SOURCE_SET_NAME)

        ss.compileClasspath += main.output
        ss.runtimeClasspath += main.output

        listOf(
          ss.implementationConfigurationName to main.implementationConfigurationName,
          ss.runtimeOnlyConfigurationName to main.runtimeOnlyConfigurationName
        ).forEach { (integrationConfig, mainConfig) ->

          target.configurations.named(integrationConfig) {
            it.extendsFrom(target.configurations.getByName(mainConfig))
          }
        }
      }

    // The `compileOnlyApi` configuration is added by the `java-library` plugin,
    // which is applied by the kotlin-jvm plugin.
    target.pluginManager.withPlugin("java-library") {
      val ss = gradleTestSourceSet.get()

      val main = target.javaSourceSet(SourceSet.MAIN_SOURCE_SET_NAME)
      target.configurations.getByName(ss.compileOnlyConfigurationName)
        .extendsFrom(target.configurations.getByName(main.compileOnlyApiConfigurationName))
    }

    val gradleTestTask = target.tasks
      .register(GRADLE_TEST, Test::class.java) { task ->

        task.group = "verification"
        task.description = "tests the '$GRADLE_TEST' source set"

        task.useJUnitPlatform()

        val javaSourceSet = gradleTestSourceSet.get()

        task.testClassesDirs = javaSourceSet.output.classesDirs
        task.classpath = javaSourceSet.runtimeClasspath
        task.inputs.files(javaSourceSet.allSource)

        task.dependsOn(target.rootProject.tasks.named(PUBLISH_TO_BUILD_M2))
      }

    target.rootProject.allprojects.forEach { anyProject ->
      anyProject.plugins.withType(PublishingPlugin::class.java).configureEach {
        setUpPublishToBuildM2(anyProject)
      }
      // setUpPublishToBuildM2(target)
    }

    // Make `check` depend upon `gradleTest`
    target.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).dependsOn(gradleTestTask)

    // Make the IDE treat `src/gradleTest/[java|kotlin]` as a test source directory.
    target.extensions.configure(IdeaModel::class.java) { idea ->
      idea.module { module ->
        module.testSources.from(gradleTestSourceSet.map { it.allSource.srcDirs })
      }
    }
  }

  /**
   * Registers this [target]'s version of the `publishToBuildM2`
   * task and adds it as a dependency to the root project's version.
   */
  private fun setUpPublishToBuildM2(target: Project) {

    val buildM2Dir = target.rootProject.layout.buildDirectory.dir("m2")

    target.gradlePublishingExtension.repositories { repositories ->
      repositories.mavenLocal {
        it.name = "buildM2"
        it.setUrl(buildM2Dir)
      }
    }

    val realPublishTask = "publishAllPublicationsToBuildM2Repository"

    val publishToBuildM2 = target.tasks.register(PUBLISH_TO_BUILD_M2) {
      it.group = "Publishing"
      it.description =
        "Delegates to the $realPublishTask task on projects where publishing is enabled."

      it.inputs.dir(buildM2Dir)

      it.dependsOn(realPublishTask)

      // Don't generate javadoc for integration tests.
      target.extras["skipDokka"] = true
    }

    target.rootProject.tasks.registerOnce<DefaultTask>(PUBLISH_TO_BUILD_M2) {
      it.group = "Publishing"
      it.description = "Hook for delegating to '$PUBLISH_TO_BUILD_M2' in all subprojects"
    }

    if (!target.isRootProject()) {
      target.rootProject.tasks.named(PUBLISH_TO_BUILD_M2).dependsOn(publishToBuildM2)
    }
  }

  private fun Project.javaSourceSet(name: String): SourceSet {
    return javaExtension.sourceSets.getByName(name)
  }

  public companion object {
    private const val GRADLE_TEST = "gradleTest"
    internal const val PUBLISH_TO_BUILD_M2 = "publishToBuildM2"
  }
}

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

internal val Project.mavenPublications: NamedDomainObjectSet<MavenPublication>
  get() = gradlePublishingExtension.publications.withType(MavenPublication::class.java)
