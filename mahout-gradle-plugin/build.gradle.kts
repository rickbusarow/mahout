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

import com.rickbusarow.kgx.library
import com.rickbusarow.kgx.libsCatalog
import com.rickbusarow.kgx.pluginId
import com.rickbusarow.kgx.version

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.poko)
  alias(libs.plugins.buildconfig)
}

if (rootProject.name == "mahout") {
  apply(plugin = "com.rickbusarow.mahout.jvm-module")
}

buildConfig {
  packageName.set("com.rickbusarow.mahout")
  useKotlinOutput {
    internalVisibility = true
  }

  listOf<Triple<String, List<String>, (String) -> String>>(
    Triple("Versions", libsCatalog.versionAliases) { libsCatalog.version(it) },
    Triple("PluginIds", libsCatalog.pluginAliases) { libsCatalog.pluginId(it) },
    Triple("Libs", libsCatalog.libraryAliases) { libsCatalog.library(it).get().toString() },
    Triple("Modules", libsCatalog.libraryAliases) {
      libsCatalog.library(it).get().module.toString()
    }
  )
    .forEach { (className, aliases, value) ->
      forClass(packageName = "com.rickbusarow.mahout.deps", className = className) {
        for (alias in aliases) {
          buildConfigField(alias.replace('.', '-'), value(alias))
        }
      }
    }
}

kotlin {
  explicitApi()
}

gradlePlugin {
  plugins {
    create("composite") {
      id = "com.rickbusarow.mahout.composite"
      implementationClass = "com.rickbusarow.mahout.composite.CompositePlugin"
    }
    create("jvm") {
      id = "com.rickbusarow.mahout.jvm-module"
      implementationClass = "com.rickbusarow.mahout.KotlinJvmModulePlugin"
    }
    create("kmp") {
      id = "com.rickbusarow.mahout.kmp-module"
      implementationClass = "com.rickbusarow.mahout.KotlinMultiplatformModulePlugin"
    }
    create("root") {
      id = "com.rickbusarow.mahout.root"
      implementationClass = "com.rickbusarow.mahout.RootPlugin"
    }

    create("curator") {
      id = "com.rickbusarow.mahout.curator"
      implementationClass = "com.rickbusarow.mahout.curator.CuratorPlugin"
    }

    fun convention(suffix: String, relativeClassName: String) {
      create("convention.$suffix") {
        id = "com.rickbusarow.mahout.$suffix"
        implementationClass = "com.rickbusarow.mahout.$relativeClassName"
      }
    }

    convention("ben-manes", "conventions.BenManesVersionsPlugin")
    convention("check", "conventions.CheckPlugin")
    convention("clean", "conventions.CleanPlugin")
    convention("dependency-guard", "conventions.DependencyGuardConventionPlugin")
    convention("detekt", "conventions.DetektConventionPlugin")
    convention("dokkatoo", "dokka.DokkatooConventionPlugin")
    convention("dokka-versioning", "conventions.DokkaVersionArchivePlugin")
    convention("github-release", "conventions.GitHubReleasePlugin")
    convention("integration-tests", "conventions.IntegrationTestsConventionPlugin")
    // convention("kotlin-jvm", "conventions.KotlinJvmConventionPlugin")
    convention("ktlint", "conventions.KtLintConventionPlugin")
    convention("spotless", "conventions.SpotlessConventionPlugin")
    convention("test", "conventions.TestConventionPlugin")
  }
}

dependencies {

  api(project(":mahout-api"))
  api(project(":mahout-core"))

  compileOnly(gradleApi())

  compileOnly(project(":mahout-settings-annotations"))

  implementation(libs.benManes.versions)
  implementation(libs.breadmoirai.github.release)
  implementation(libs.detekt.gradle)
  implementation(libs.diffplug.spotless)
  implementation(libs.dokka.core)
  implementation(libs.dokka.gradle)
  implementation(libs.dokka.versioning)
  implementation(libs.dokkatoo.plugin)
  implementation(libs.drewHamilton.poko.gradle.plugin)
  implementation(libs.dropbox.dependencyGuard)
  implementation(libs.ec4j.core)
  implementation(libs.johnrengelman.shadowJar)
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.kotlin.gradle.plugin.api)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlinx.binaryCompatibility)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.rickBusarow.doks)
  implementation(libs.rickBusarow.kgx)
  implementation(libs.rickBusarow.ktlint)
  implementation(libs.rickBusarow.moduleCheck.gradle.plugin) {
    exclude(group = "org.jetbrains.kotlin")
  }
  implementation(libs.vanniktech.publish.plugin)

  ksp(project(":mahout-settings-generator"))

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.jupiter.engine)
  testImplementation(libs.junit.jupiter.params)
  testImplementation(libs.kase)
  testImplementation(libs.kotest.assertions.api)
  testImplementation(libs.kotest.assertions.core.jvm)
  testImplementation(libs.kotest.assertions.shared)
}
