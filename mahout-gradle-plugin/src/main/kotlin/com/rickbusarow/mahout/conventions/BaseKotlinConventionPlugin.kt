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

package com.rickbusarow.mahout.conventions

import com.rickbusarow.kgx.javaExtension
import com.rickbusarow.mahout.config.jvmTargetInt
import com.rickbusarow.mahout.config.jvmToolchainInt
import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.core.Color
import com.rickbusarow.mahout.core.Color.Companion.colorized
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
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

    val kotlinExtension = target.extensions
      .getByType(KotlinProjectExtension::class.java)

    val javaSettings = target.mahoutProperties.java

    kotlinExtension.jvmToolchain(javaSettings.jvmToolchainInt.get())

    configureKotlinOptions(target, kotlinExtension)

    kotlinExtension.sourceSets.configureEach { sourceSet ->
      sourceSet.kotlin.srcDirs("src/${sourceSet.name}/kotlin")
    }

    target.tasks.register("buildAll") { buildAll ->
      buildAll.dependsOn(kotlinExtension.targets.map { it.artifactsTaskName })
    }

    target.plugins.withId("java") {
      target.tasks.withType(JavaCompile::class.java).configureEach { task ->

        println(
          "${task.path}  -- set release to ${javaSettings.jvmTargetInt.get()}",
          color = Color.CYAN
        )

        task.options.release.set(javaSettings.jvmTargetInt.get())
      }

      target.javaExtension.run {

        targetCompatibility = JavaVersion.toVersion(javaSettings.jvmTarget.get())
        sourceCompatibility = JavaVersion.toVersion(javaSettings.jvmSource.get())
      }

      // fixes the error
      // 'Entry classpath.index is a duplicate but no duplicate handling strategy has been set.'
      // when executing a Jar task
      // https://github.com/gradle/gradle/issues/17236
      target.tasks.withType(Jar::class.java).configureEach { task ->
        task.duplicatesStrategy = DuplicatesStrategy.INCLUDE
      }
    }
  }

  // TODO (rbusarow) delete me
  @Deprecated("debugging -- delete me")
  internal fun println(vararg msg: Any?, color: Color = Color.LIGHT_GREEN) {
    check(System.getenv("CI") == null) { "delete me" }
    for (m in msg) kotlin.io.println(m.toString().colorized(color))
  }

  private fun configureKotlinOptions(target: Project, extension: KotlinProjectExtension) {
    target.tasks.withType(KotlinJvmCompile::class.java).configureEach { task ->

      // TODO (rbusarow) delete me
      check(System.getenv("CI") == null) { "delete me" }.run {

        val jt = target.mahoutProperties.java.jvmTarget.get()
        println(
          "${task.path}  -- set kotlinOptions.jvmTarget to $jt",
          color = Color.MAGENTA
        )
      }

      task.kotlinOptions.jvmTarget = target.mahoutProperties.java.jvmTarget.get()
    }
    target.tasks.withType(KotlinCompileDsl::class.java).configureEach { task ->
      task.kotlinOptions {

        // options.allWarningsAsErrors.set(extension.allWarningsAsErrors.orElse(false))

        val kotlinMajor = target.mahoutProperties.kotlin.apiLevel.orNull
        if (kotlinMajor != null) {
          languageVersion = kotlinMajor
          apiVersion = kotlinMajor
        }

        @Suppress("SpellCheckingInspection")
        freeCompilerArgs += buildList {
          add("-Xinline-classes")
          add("-Xcontext-receivers")

          val sourceSetName = (task as? BaseKotlinCompile)?.sourceSetName?.orNull

          val shouldBeStrict = when {
            extension.explicitApi != Strict -> false
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
