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

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.poko)
}

if (rootProject.name == "antipasto") {
  apply(plugin = "com.rickbusarow.antipasto.jvm-module")
}

gradlePlugin {
  plugins {
    create("composite") {
      id = "com.rickbusarow.antipasto.composite"
      implementationClass = "com.rickbusarow.antipasto.CompositePlugin"
    }
    create("jvm") {
      id = "com.rickbusarow.antipasto.jvm-module"
      implementationClass = "com.rickbusarow.antipasto.KotlinJvmModulePlugin"
    }
    create("kmp") {
      id = "com.rickbusarow.antipasto.kmp-module"
      implementationClass = "com.rickbusarow.antipasto.KotlinMultiplatformModulePlugin"
    }
    create("root") {
      id = "com.rickbusarow.antipasto.root"
      implementationClass = "com.rickbusarow.antipasto.RootPlugin"
    }

    create("curator") {
      id = "com.rickbusarow.antipasto.curator"
      implementationClass = "com.rickbusarow.antipasto.curator.CuratorPlugin"
    }
    create("ben-manes") {
      id = "com.rickbusarow.antipasto.ben-manes"
      implementationClass = "com.rickbusarow.antipasto.conventions.BenManesVersionsPlugin"
    }
    create("antipasto.check") {
      id = "com.rickbusarow.antipasto.check"
      implementationClass = "com.rickbusarow.antipasto.conventions.CheckPlugin"
    }
    create("antipasto.clean") {
      id = "com.rickbusarow.antipasto.clean"
      implementationClass = "com.rickbusarow.antipasto.conventions.CleanPlugin"
    }
    create("antipasto.dependency-guard") {
      id = "com.rickbusarow.antipasto.dependency-guard"
      implementationClass = "com.rickbusarow.antipasto.conventions.DependencyGuardConventionPlugin"
    }
    create("antipasto.detekt") {
      id = "com.rickbusarow.antipasto.detekt"
      implementationClass = "com.rickbusarow.antipasto.conventions.DetektConventionPlugin"
    }
    create("antipasto.dokkatoo") {
      id = "com.rickbusarow.antipasto.dokkatoo"
      implementationClass = "com.rickbusarow.antipasto.conventions.DokkatooConventionPlugin"
    }
    create("antipasto.dokka-versioning") {
      id = "com.rickbusarow.antipasto.dokka-versioning"
      implementationClass = "com.rickbusarow.antipasto.conventions.DokkaVersionArchivePlugin"
    }
    create("antipasto.github-release") {
      id = "com.rickbusarow.antipasto.github-release"
      implementationClass = "com.rickbusarow.antipasto.conventions.GitHubReleasePlugin"
    }
    create("antipasto.integration-tests") {
      id = "com.rickbusarow.antipasto.integration-tests"
      implementationClass = "com.rickbusarow.antipasto.conventions.IntegrationTestsConventionPlugin"
    }
    create("antipasto.kotlin") {
      id = "com.rickbusarow.antipasto.kotlin"
      implementationClass = "com.rickbusarow.antipasto.conventions.KotlinJvmConventionPlugin"
    }
    create("antipasto.ktlint") {
      id = "com.rickbusarow.antipasto.ktlint"
      implementationClass = "com.rickbusarow.antipasto.conventions.KtLintConventionPlugin"
    }
    create("antipasto.spotless") {
      id = "com.rickbusarow.antipasto.spotless"
      implementationClass = "com.rickbusarow.antipasto.conventions.SpotlessConventionPlugin"
    }
    create("antipasto.test") {
      id = "com.rickbusarow.antipasto.test"
      implementationClass = "com.rickbusarow.antipasto.conventions.TestConventionPlugin"
    }
  }
}

dependencies {

  api(libs.breadmoirai.github.release)
  api(libs.integration.test) {
    exclude(group = "org.jetbrains.kotlin")
  }
  api(libs.rickBusarow.doks)
  api(libs.rickBusarow.ktlint)
  api(libs.rickBusarow.kgx)

  compileOnly(gradleApi())
  implementation(libs.kotlinx.serialization.json)

  implementation(libs.benManes.versions)
  implementation(libs.detekt.gradle)
  implementation(libs.diffplug.spotless)
  implementation(libs.dokka.core)
  implementation(libs.dokka.gradle)
  implementation(libs.dokka.versioning)
  implementation(libs.dokkatoo.plugin)
  implementation(libs.dropbox.dependencyGuard)
  implementation(libs.johnrengelman.shadowJar)
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.kotlin.gradle.plugin.api)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlinx.binaryCompatibility)
  implementation(libs.poko.gradle.plugin)
  implementation(libs.vanniktech.publish)
  implementation(libs.rickBusarow.moduleCheck.gradle.plugin) {
    exclude(group = "org.jetbrains.kotlin")
  }
}
