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
import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.mahoutExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.bundling.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import java.io.Serializable

/** */
public interface KotlinJvmExtension : KotlinExtension

/** */
public interface KotlinMultiplatformExtension : KotlinExtension

/** */
public interface KotlinExtension : Serializable {

  @Suppress("UndocumentedPublicProperty")
  public val allWarningsAsErrors: Property<Boolean>

  @Suppress("UndocumentedPublicProperty")
  public val explicitApi: Property<Boolean>
}

/** */
public abstract class BaseKotlinConventionPlugin : Plugin<Project> {

  private val KotlinProjectExtension.targets: Iterable<KotlinTarget>
    get() = when (this) {
      is KotlinSingleTargetExtension<*> -> listOf(this.target)
      is KotlinMultiplatformExtension -> targets
      else -> error("Unexpected 'kotlin' extension $this")
    }

  override fun apply(target: Project) {

    target.plugins.applyOnce<JdkVersionsConventionPlugin>()
    val extension = (target.mahoutExtension as HasKotlinSubExtension).kotlin

    // val kotlinExtension = target.extensions.getByType(KotlinExtension::class.java)

    val kotlinExtensionJB = target.extensions
      .getByType(KotlinProjectExtension::class.java)

    configureKotlinOptions(target, extension, kotlinExtensionJB)

    target.tasks.register("buildAll") { buildAll ->
      buildAll.dependsOn(kotlinExtensionJB.targets.map { it.artifactsTaskName })
    }

    target.plugins.withId("java") {

      // fixes the error
      // 'Entry classpath.index is a duplicate but no duplicate handling strategy has been set.'
      // when executing a Jar task
      // https://github.com/gradle/gradle/issues/17236
      target.tasks.withType(Jar::class.java).configureEach { task ->
        task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }
    }
  }

  private fun configureKotlinOptions(
    target: Project,
    extension: KotlinSubExtension,
    kotlinExtensionJB: KotlinProjectExtension
  ) {

    target.tasks.withType(KotlinCompilationTask::class.java).configureEach { task ->

      if (extension.explicitApi.orNull == true) {
        kotlinExtensionJB.explicitApi()
      }

      task.compilerOptions {

        allWarningsAsErrors.set(extension.allWarningsAsErrors.orElse(false))

        val kotlinMajor = target.mahoutProperties.kotlin.apiLevel.map {
          KotlinVersion.fromVersion(it)
        }
        languageVersion.set(kotlinMajor)
        apiVersion.set(kotlinMajor)

        @Suppress("SpellCheckingInspection")
        freeCompilerArgs.addAll(
          "-Xinline-classes",
          "-Xcontext-receivers"
        )
      }
    }
  }
}
