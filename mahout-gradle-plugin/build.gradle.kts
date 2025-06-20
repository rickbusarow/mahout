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

import com.rickbusarow.kgx.library
import com.rickbusarow.kgx.libsCatalog
import com.rickbusarow.kgx.pluginId
import com.rickbusarow.kgx.version

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.drewHamilton.poko)

  alias(libs.plugins.plugin.publish)
  alias(libs.plugins.vanniktech.publish.base) apply false
  alias(libs.plugins.buildconfig)
  id("com.rickbusarow.mahout.java-gradle-plugin")
  idea
}

val plugins = with(gradlePlugin.plugins) {
  listOf(
    register("composite") {
      this.id = "com.rickbusarow.mahout.composite"
      this.implementationClass = "com.rickbusarow.mahout.composite.CompositePlugin"
      this.description =
        "Propagates unqualified task requests from the root build to all included builds"
      this.tags.addAll("convention-plugin", "kotlin")
    },
    register("curator") {
      id = "com.rickbusarow.mahout.curator"
      implementationClass = "com.rickbusarow.mahout.curator.CuratorPlugin"
      this@register.description = "Verifies the consistency of a project's published artifacts"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("gradle-test") {
      id = "com.rickbusarow.mahout.gradle-test"
      implementationClass = "com.rickbusarow.mahout.conventions.GradleTestsPlugin"
      this@register.description = "Configures a source set for Gradle integration tests"
      tags.addAll("convention-plugin", "kotlin", "testing", "gradle-plugin", "plugin", "kotlin-jvm")
    },
    register("java-gradle-plugin") {
      id = "com.rickbusarow.mahout.java-gradle-plugin"
      implementationClass = "com.rickbusarow.mahout.GradlePluginModulePlugin"
      this@register.description = "Convention plugin for a java-gradle-plugin project"
      tags.addAll("convention-plugin", "kotlin", "plugin", "java", "jvm", "kotlin-jvm")
    },
    register("kotlin-jvm-module") {
      id = "com.rickbusarow.mahout.kotlin-jvm-module"
      implementationClass = "com.rickbusarow.mahout.KotlinJvmModulePlugin"
      this@register.description = "Convention plugin for a Kotlin JVM project"
      tags.addAll("convention-plugin", "kotlin", "java", "jvm", "kotlin-jvm")
    },
    register("kotlin-multiplatform-module") {
      id = "com.rickbusarow.mahout.kotlin-multiplatform-module"
      implementationClass = "com.rickbusarow.mahout.KotlinMultiplatformModulePlugin"
      this@register.description = "Convention plugin for a Kotlin Multiplatform project"
      tags.addAll("convention-plugin", "kotlin", "multiplatform", "kotlin-multiplatform")
    },
    register("root") {
      id = "com.rickbusarow.mahout.root"
      implementationClass = "com.rickbusarow.mahout.RootPlugin"
      this@register.description = "Convention plugin for the root project of a multi-module build"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.ben-manes") {
      id = "com.rickbusarow.mahout.convention.ben-manes"
      implementationClass = "com.rickbusarow.mahout.conventions.BenManesVersionsPlugin"
      this@register.description = "configures the Ben Manes versions plugin"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.clean") {
      id = "com.rickbusarow.mahout.convention.clean"
      implementationClass = "com.rickbusarow.mahout.conventions.CleanPlugin"
      this@register.description =
        "adds tasks to clean up empty directories and orphaned project directories"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.dependency-guard") {
      id = "com.rickbusarow.mahout.convention.dependency-guard"
      implementationClass = "com.rickbusarow.mahout.conventions.DependencyGuardConventionPlugin"
      this@register.description = "configures the dependency-guard plugin"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.detekt") {
      id = "com.rickbusarow.mahout.convention.detekt"
      implementationClass = "com.rickbusarow.mahout.conventions.DetektConventionPlugin"
      this@register.description = "configures the detekt plugin"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.dokka-versioning") {
      id = "com.rickbusarow.mahout.convention.dokka-versioning"
      implementationClass = "com.rickbusarow.mahout.conventions.DokkaVersionArchivePlugin"
      this@register.description = "automates the archival of versioned api documentation"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.dokkatoo") {
      id = "com.rickbusarow.mahout.convention.dokkatoo"
      implementationClass = "com.rickbusarow.mahout.dokka.DokkatooConventionPlugin"
      this@register.description = "configures the dokkatoo plugin"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.fix") {
      id = "com.rickbusarow.mahout.convention.fix"
      implementationClass = "com.rickbusarow.mahout.conventions.FixPlugin"
      this@register.description =
        "adds tasks to apply all baseline and auto-correct tasks at once"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.feature-variants") {
      id = "com.rickbusarow.mahout.convention.feature-variants"
      implementationClass = "com.rickbusarow.mahout.variants.FeatureVariantsPlugin"
      this@register.description = "configures feature variants"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.github-release") {
      id = "com.rickbusarow.mahout.convention.github-release"
      implementationClass = "com.rickbusarow.mahout.conventions.GitHubReleasePlugin"
      this@register.description = "configures the GitHub release plugin"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.kotlin-jvm") {
      id = "com.rickbusarow.mahout.convention.kotlin-jvm"
      implementationClass = "com.rickbusarow.mahout.conventions.KotlinJvmConventionPlugin"
      this@register.description = "configures Kotlin JVM projects"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.ktlint") {
      id = "com.rickbusarow.mahout.convention.ktlint"
      implementationClass = "com.rickbusarow.mahout.conventions.KtLintConventionPlugin"
      this@register.description = "configures the ktlint plugin"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.spotless") {
      id = "com.rickbusarow.mahout.convention.spotless"
      implementationClass = "com.rickbusarow.mahout.conventions.SpotlessConventionPlugin"
      this@register.description = "configures the spotless plugin"
      tags.addAll("convention-plugin", "kotlin")
    },
    register("convention.test") {
      id = "com.rickbusarow.mahout.convention.test"
      implementationClass = "com.rickbusarow.mahout.conventions.TestConventionPlugin"
      this@register.description = "applies common test configurations"
      tags.addAll("convention-plugin", "kotlin")
    }
  )
}

mahout {
  kotlin {
    explicitApi = true
  }

  publishing {
    pluginMaven(
      artifactId = "mahout-gradle-plugin",
      pomDescription = "Convention plugins for Gradle builds"
    )
    for (plugin in plugins) {
      publishPlugin(plugin)
    }
  }
  gradleTests {}
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

  sourceSets.named(mahout.gradleTests.sourceSetName.get()) {
    className("GradleTestBuildConfig")
    buildConfigField("mahoutVersion", project.version.toString())
    buildConfigField("buildM2Dir", mahout.gradleTests.gradleTestM2Dir.asFile)
  }
}

val gradleTestImplementation: Configuration by configurations.getting

dependencies {

  api(project(":mahout-api"))
  api(project(":mahout-core"))

  compileOnly(libs.rickBusarow.moduleCheck.gradle.plugin) {
    exclude(group = "org.jetbrains.kotlin")
  }

  compileOnly(gradleApi())
  compileOnly(libs.dokka.core)
  compileOnly(libs.dokka.gradle)
  compileOnly(libs.dokka.versioning)
  compileOnly(libs.drewHamilton.poko.gradle.plugin)
  compileOnly(libs.gradleup.shadow)
  compileOnly(libs.vanniktech.publish.nexus)

  compileOnly(libs.kotlin.gradle.plugin)
  compileOnly(libs.kotlin.gradle.plugin.api)
  compileOnly(libs.kotlin.reflect)

  compileOnly(project(":mahout-settings-annotations"))

  gradleTestImplementation(libs.junit.jupiter)
  gradleTestImplementation(libs.junit.jupiter.api)
  gradleTestImplementation(libs.junit.jupiter.engine)
  gradleTestImplementation(libs.junit.jupiter.params)
  gradleTestImplementation(libs.junit.platform.launcher)
  gradleTestImplementation(libs.kase)
  gradleTestImplementation(libs.kase.gradle)
  gradleTestImplementation(libs.kase.gradle.dsl)
  gradleTestImplementation(libs.kotest.assertions.api)
  gradleTestImplementation(libs.kotest.assertions.core.jvm)
  gradleTestImplementation(libs.kotest.assertions.shared)

  implementation(libs.benManes.versions)
  implementation(libs.breadmoirai.github.release)
  implementation(libs.detekt.gradle)
  implementation(libs.diffplug.spotless)
  implementation(libs.dokkatoo.plugin)
  implementation(libs.dropbox.dependencyGuard)
  implementation(libs.ec4j.core)
  implementation(libs.kotlinx.binaryCompatibility)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.rickBusarow.doks)
  implementation(libs.rickBusarow.kgx)
  implementation(libs.rickBusarow.ktlint)
  implementation(libs.vanniktech.publish.plugin)

  compileOnly(libs.square.kotlinPoet)

  ksp(project(":mahout-settings-generator"))

  testImplementation(gradleApi())
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.jupiter.engine)
  testImplementation(libs.junit.jupiter.params)
  testImplementation(libs.kase)
  testImplementation(libs.kotest.assertions.api)
  testImplementation(libs.kotest.assertions.core.jvm)
  testImplementation(libs.kotest.assertions.shared)
  testImplementation(libs.kotlin.gradle.plugin)
  testImplementation(libs.kotlin.gradle.plugin.api)
  testImplementation(libs.kotlin.reflect)
}
