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

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.poko)
}

if (rootProject.name == "lattice") {
  apply(plugin = "com.rickbusarow.lattice.jvm-module")
}

gradlePlugin {
  plugins {
    create("composite") {
      id = "com.rickbusarow.lattice.composite"
      implementationClass = "com.rickbusarow.lattice.CompositePlugin"
    }
    create("jvm") {
      id = "com.rickbusarow.lattice.jvm-module"
      implementationClass = "com.rickbusarow.lattice.KotlinJvmModulePlugin"
    }
    create("kmp") {
      id = "com.rickbusarow.lattice.kmp-module"
      implementationClass = "com.rickbusarow.lattice.KotlinMultiplatformModulePlugin"
    }
    create("root") {
      id = "com.rickbusarow.lattice.root"
      implementationClass = "com.rickbusarow.lattice.RootPlugin"
    }

    create("curator") {
      id = "com.rickbusarow.lattice.curator"
      implementationClass = "com.rickbusarow.lattice.curator.CuratorPlugin"
    }

    fun convention(suffix: String, simpleClassName: String) {
      create("convention.$suffix") {
        id = "com.rickbusarow.lattice.$suffix"
        implementationClass = "com.rickbusarow.lattice.conventions.$simpleClassName"
      }
    }

    convention("ben-manes", "BenManesVersionsPlugin")
    convention("check", "CheckPlugin")
    convention("clean", "CleanPlugin")
    convention("dependency-guard", "DependencyGuardConventionPlugin")
    convention("detekt", "DetektConventionPlugin")
    convention("dokkatoo", "DokkatooConventionPlugin")
    convention("dokka-versioning", "DokkaVersionArchivePlugin")
    convention("github-release", "GitHubReleasePlugin")
    convention("integration-tests", "IntegrationTestsConventionPlugin")
    convention("kotlin-jvm", "KotlinJvmConventionPlugin")
    convention("ktlint", "KtLintConventionPlugin")
    convention("spotless", "SpotlessConventionPlugin")
    convention("test", "TestConventionPlugin")
  }
}

dependencies {

  compileOnly(gradleApi())

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
  implementation(libs.integration.test) {
    exclude(group = "org.jetbrains.kotlin")
  }
  implementation(libs.johnrengelman.shadowJar)
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.kotlin.gradle.plugin.api)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlinx.binaryCompatibility)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.picnic)
  implementation(libs.rickBusarow.doks)
  implementation(libs.rickBusarow.kgx)
  implementation(libs.rickBusarow.ktlint)
  implementation(libs.rickBusarow.moduleCheck.gradle.plugin) {
    exclude(group = "org.jetbrains.kotlin")
  }
  implementation(libs.vanniktech.publish.plugin)
}
