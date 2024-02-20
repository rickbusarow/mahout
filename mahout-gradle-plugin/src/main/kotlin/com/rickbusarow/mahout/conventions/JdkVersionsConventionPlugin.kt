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
import com.rickbusarow.kgx.withJavaBasePlugin
import com.rickbusarow.kgx.withKotlinJvmPlugin
import com.rickbusarow.mahout.config.JvmVersion.Companion.javaLanguageVersion
import com.rickbusarow.mahout.config.JvmVersion.Companion.jvmTargetKotlinGradle
import com.rickbusarow.mahout.config.JvmVersion.Companion.major
import com.rickbusarow.mahout.config.mahoutProperties
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

@Suppress("UndocumentedPublicClass")
public abstract class JdkVersionsConventionPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    val javaSettings = target.mahoutProperties.java

    target.plugins.withJavaBasePlugin {
      val javaExtension = target.javaExtension

      javaExtension.toolchain {
        it.languageVersion.set(javaSettings.jvmToolchain.javaLanguageVersion)
      }

      javaExtension.targetCompatibility = javaSettings.jvmTarget.get().javaVersion
      javaExtension.sourceCompatibility = javaSettings.jvmSource.get().javaVersion

      target.tasks.withType(JavaCompile::class.java).configureEach { task ->
        task.options.release.set(javaSettings.jvmTarget.major.get())
      }
    }

    for (it in javaSettings.jvmTarget.get().major..javaSettings.jvmToolchain.get().major) {
      target.tasks.register("testJdk$it", Test::class.java) { task ->
        task.javaLauncher.set(target.javaLauncherFor(JavaLanguageVersion.of(it)))
      }
    }

    target.tasks.withType(Test::class.java).configureEach { task ->
      task.javaLauncher.set(target.javaLauncherFor(javaSettings.jvmTarget.javaLanguageVersion))
    }

    target.plugins.withKotlinJvmPlugin {
      val kotlinExtension = target.extensions
        .getByType(KotlinJvmProjectExtension::class.java)

      kotlinExtension.compilerOptions {
        jvmTarget.set(javaSettings.jvmTarget.jvmTargetKotlinGradle)
      }
    }
  }

  private fun Project.javaLauncherFor(javaLanguageVersion: Provider<JavaLanguageVersion>) =
    extensions.getByType(JavaToolchainService::class.java)
      .launcherFor { it.languageVersion.set(javaLanguageVersion) }
}
