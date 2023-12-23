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

import com.rickbusarow.kgx.javaExtension
import com.rickbusarow.lattice.config.jvmTargetInt
import com.rickbusarow.lattice.config.jvmToolchainInt
import com.rickbusarow.lattice.config.latticeProperties
import com.rickbusarow.lattice.latticeExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.targets
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.io.Serializable
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile as KotlinCompileDsl

public interface KotlinJvmExtension : KotlinExtension

public interface KotlinMultiplatformExtension : KotlinExtension

public interface KotlinExtension : Serializable {

  @Suppress("UndocumentedPublicProperty")
  public val allWarningsAsErrors: Property<Boolean>

  @Suppress("UndocumentedPublicProperty")
  public val explicitApi: Property<Boolean>
}

public abstract class BaseKotlinConventionPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    val extension = (target.latticeExtension as HasKotlinSubExtension).kotlin

    val jetbrainsExtension = target.kotlinExtension
    jetbrainsExtension.jvmToolchain(target.latticeProperties.java.jvmToolchainInt.get())

    configureKotlinOptions(target, extension)

    jetbrainsExtension.sourceSets.configureEach { sourceSet ->
      sourceSet.kotlin.srcDirs("src/${sourceSet.name}/kotlin")
    }
    target.tasks.register("buildTests") { buildTests ->
      buildTests.dependsOn(jetbrainsExtension.targets.map { it.artifactsTaskName })
    }
    target.tasks.register("buildAll") { buildAll ->
      buildAll.dependsOn(jetbrainsExtension.targets.map { it.artifactsTaskName })
    }

    target.plugins.withId("java") {
      target.tasks.withType(JavaCompile::class.java).configureEach { task ->
        task.options.release.set(target.latticeProperties.java.jvmTargetInt.get())
      }

      target.javaExtension.sourceCompatibility = JavaVersion.toVersion(
        target.latticeProperties.java.jvmTarget.get()
      )

      // fixes the error
      // 'Entry classpath.index is a duplicate but no duplicate handling strategy has been set.'
      // when executing a Jar task
      // https://github.com/gradle/gradle/issues/17236
      target.tasks.withType(Jar::class.java).configureEach { task ->
        task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }
    }
  }

  private fun configureKotlinOptions(target: Project, extension: KotlinSubExtension) {

    target.tasks.withType(KotlinJvmCompile::class.java).configureEach { task ->
      task.kotlinOptions.jvmTarget = target.latticeProperties.java.jvmTarget.get()
    }
    target.tasks.withType(KotlinCompileDsl::class.java).configureEach { task ->

      task.kotlinOptions {

        options.allWarningsAsErrors.set(extension.allWarningsAsErrors.orElse(false))

        val kotlinMajor = target.latticeProperties.kotlin.apiLevel.orNull
        if (kotlinMajor != null) {
          languageVersion = kotlinMajor
          apiVersion = kotlinMajor
        }
        val jvmTarget = target.latticeProperties.java.jvmTarget.orNull
        if (jvmTarget != null) {
          (this as? KotlinJvmOptions)?.jvmTarget = jvmTarget
        }

        @Suppress("SpellCheckingInspection")
        freeCompilerArgs += buildList {
          add("-Xinline-classes")
          add("-Xcontext-receivers")

          val explicitApiEnabled = target.latticeProperties.kotlin.explicitApi.orNull == true
          if (explicitApiEnabled) {
            add("-Xexplicit-api=strict")
          }
        }
      }
    }
  }
}
