/*
 * Copyright (C) 2023 Rick Busarow
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
import com.rickbusarow.lattice.core.JDK_INT
import com.rickbusarow.lattice.core.JVM_TARGET
import com.rickbusarow.lattice.core.JVM_TARGET_INT
import com.rickbusarow.lattice.core.KOTLIN_API
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
import org.jetbrains.kotlin.gradle.tasks.BaseKotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.io.Serializable
import kotlin.jvm.java
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile as KotlinCompileDsl

@Suppress("UndocumentedPublicClass")
public interface KotlinJvmExtension : KotlinExtension

@Suppress("UndocumentedPublicClass")
public interface KotlinMultiplatformExtension : KotlinExtension

@Suppress("UndocumentedPublicClass")
public interface KotlinExtension : Serializable {

  @Suppress("UndocumentedPublicProperty")
  public val allWarningsAsErrors: Property<Boolean>

  @Suppress("UndocumentedPublicProperty")
  public val explicitApi: Property<Boolean>
}

@Suppress("UndocumentedPublicClass")
public abstract class BaseKotlinConventionPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    val extension = target.extensions.getByType(KotlinExtension::class.java)

    val jetbrainsExtension = target.kotlinExtension
    jetbrainsExtension.jvmToolchain(target.JDK_INT)

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
        task.options.release.set(target.JVM_TARGET_INT)
      }

      target.javaExtension.sourceCompatibility = JavaVersion.toVersion(target.JVM_TARGET)

      // fixes the error
      // 'Entry classpath.index is a duplicate but no duplicate handling strategy has been set.'
      // when executing a Jar task
      // https://github.com/gradle/gradle/issues/17236
      target.tasks.withType(Jar::class.java).configureEach { task ->
        task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }
    }
  }

  private fun configureKotlinOptions(target: Project, extension: KotlinExtension) {
    target.tasks.withType(KotlinJvmCompile::class.java).configureEach { task ->
      task.kotlinOptions.jvmTarget = target.JVM_TARGET
    }
    target.tasks.withType(KotlinCompileDsl::class.java).configureEach { task ->
      task.kotlinOptions {

        options.allWarningsAsErrors.set(extension.allWarningsAsErrors.orElse(false))

        val kotlinMajor = target.KOTLIN_API
        languageVersion = kotlinMajor
        apiVersion = kotlinMajor

        (this as? KotlinJvmOptions)?.jvmTarget = target.JVM_TARGET

        @Suppress("SpellCheckingInspection")
        freeCompilerArgs += buildList {
          add("-Xinline-classes")
          add("-Xcontext-receivers")

          val sourceSetName = (task as? BaseKotlinCompile)?.sourceSetName?.orNull

          val shouldBeStrict = when {
            extension.explicitApi.orNull == false -> false
            sourceSetName == "test" -> false
            sourceSetName == null -> false
            else -> true
          }
          if (shouldBeStrict) {
            add("-Xexplicit-api=strict")
          }
        }
      }
    }
  }
}
