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
import com.rickbusarow.kgx.property
import com.rickbusarow.kgx.withJavaGradlePluginPlugin
import com.rickbusarow.mahout.api.MahoutTask
import com.rickbusarow.mahout.api.SubExtension
import com.rickbusarow.mahout.api.SubExtensionInternal
import com.rickbusarow.mahout.core.stdlib.capitalize
import com.rickbusarow.mahout.deps.Versions
import com.rickbusarow.mahout.mahoutExtensionAs
import com.rickbusarow.mahout.publishing.gradlePublishingExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.provider.Property
import org.gradle.api.publish.Publication
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSet
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.testing.base.TestingExtension
import javax.inject.Inject

/** */
public interface HasGradleTestsSubExtension {

  /** configures the Gradle tests suite for this project. */
  public val gradleTests: GradleTestsSubExtension

  /**
   * Adds a `gradleTest` source set to the project.
   *
   * @see GradleTestsPlugin
   */
  @Suppress("UndocumentedPublicFunction")
  public fun Project.gradleTests(action: Action<in GradleTestsSubExtension>) {
    plugins.apply(GradleTestsPlugin::class.java)
    gradleTests.configure(action)
  }
}

internal abstract class DefaultHasGradleTestsSubExtension @Inject constructor(
  final override val objects: ObjectFactory
) : AbstractHasSubExtension(), HasGradleTestsSubExtension {

  override val gradleTests: GradleTestsSubExtension
    by subExtension(DefaultGradleTestsSubExtension::class)
}

/** */
public interface GradleTestsSubExtension : SubExtension<GradleTestsSubExtension> {

  /**
   * Corresponds to `./src/<sourceSetName>/`, and holds the Gradle tests.
   *
   * **default**: `gradleTest`
   */
  public val sourceSetName: Property<String>

  /**
   * The name of the local Maven (`.m2`) repository used for Gradle TestKit tests.
   *
   * **default**: `gradleTestM2`
   */
  public val repositoryName: Property<String>

  /**
   * The local Maven repository directory for Gradle TestKit tests.
   *
   * **default**: `<rootProject>/build/gradle-test-m2`
   */
  public val gradleTestM2Dir: DirectoryProperty
}

internal abstract class DefaultGradleTestsSubExtension @Inject constructor(
  final override val objects: ObjectFactory,
  target: Project
) : AbstractHasSubExtension(), GradleTestsSubExtension, SubExtensionInternal {

  override val sourceSetName: Property<String> = objects.property("gradleTest")

  override val repositoryName: Property<String> = objects.property("gradleTestM2")

  override val gradleTestM2Dir: DirectoryProperty = objects.directoryProperty()
    .convention(target.rootProject.layout.buildDirectory.dir("gradle-test-m2"))
}

/**
 * This plugin will:
 * - add a `gradleTest` source set to the project.
 * - declare that source set as a "test" source set in the IDE.
 * - register a `gradleTest` task that runs the tests in the `gradleTest` source set.
 * - make the `check` task depend upon the `gradleTest` task.
 * - make the `gradleTest` task depend upon the `publishToGradleTestM2` task.
 */
public abstract class GradleTestsPlugin : Plugin<Project> {

  @Suppress("UnstableApiUsage")
  override fun apply(target: Project) {

    target.plugins.applyOnce("jvm-test-suite")

    val extension = target.mahoutExtensionAs<HasGradleTestsSubExtension>()
      .gradleTests

    val suiteName = TestSuiteName(extension.sourceSetName.get())
    val repoName = RepoName(extension.repositoryName.get())

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
    target.gradlePluginExtensionSafe { gradleExtension ->
      gradleExtension.testSourceSets(target.javaSourceSet(suiteName.value))
    }

    target.kotlinJvmExtensionSafe { kotlinExtension ->

      val compilations = kotlinExtension.target.compilations

      compilations.named(suiteName.value) {
        it.associateWith(compilations.getByName("main"))
      }
    }

    target.rootProject.allprojects { anyProject ->

      anyProject.plugins.withId("maven-publish") {
        setUpPublishToGradleTestM2(
          target = anyProject,
          repoName = repoName,
          gradleTestM2Dir = extension.gradleTestM2Dir
        )
      }

      suite.configure { st ->
        st.targets.configureEach { suiteTarget ->
          suiteTarget.testTask.configure { testTask ->
            testTask.dependsOn(anyProject.tasks.named { it == repoName.publishAllTo })
          }
        }
      }
    }

    // Make `check` depend upon `gradleTest`
    target.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME).dependsOn(suiteName.value)
  }

  /**
   * Registers this [target]'s version of the `publishToGradleTestM2`
   * task and adds it as a dependency to the root project's version.
   */
  private fun setUpPublishToGradleTestM2(
    target: Project,
    repoName: RepoName,
    gradleTestM2Dir: DirectoryProperty
  ) {

    val publishTo = repoName.publishTo
    val publishAllTo = repoName.publishAllTo

    target.gradlePublishingExtension.repositories { repositories ->
      repositories.mavenLocal {
        it.name = repoName.value
        it.setUrl(gradleTestM2Dir)
      }
    }
    target.tasks.register(publishTo) {
      it.group = "Publishing"
      it.dependsOn(publishAllTo)
    }
    target.tasks.register("${publishTo}NoDokka") {
      it.group = "Publishing"
      it.extras.set("skipDokka", true)
      it.dependsOn(publishAllTo)
    }
  }

  private fun Project.javaSourceSet(name: String): SourceSet {
    return javaExtension.sourceSets.getByName(name)
  }

  @JvmInline
  private value class RepoName(val value: String) {
    val capitalized: String get() = value.capitalize()
    val publishTo: String get() = "publishTo${capitalized}Repository"
    val publishAllTo: String get() = "publishAllPublicationsTo${capitalized}Repository"
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
