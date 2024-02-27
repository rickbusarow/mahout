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

import com.rickbusarow.kgx.propertyAs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

buildscript {
  dependencies {
    classpath(libs.kotlin.gradle.plugin)
    classpath(libs.vanniktech.publish.plugin)
    classpath(libs.rickBusarow.kgx)
  }
}

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.rickBusarow.ktlint)
}

gradlePlugin {
  plugins {
    register("conventions.dogFood") {
      id = "conventions.dogFood"
      implementationClass = "conventions.DogFoodPlugin"
    }
  }
}

dependencies {
  implementation(libs.vanniktech.publish.plugin)
  implementation(libs.rickBusarow.kgx)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlin.gradle.plugin)

  // Expose the generated version catalog API to the plugins.
  implementation(files(libs::class.java.superclass.protectionDomain.codeSource.location))
}

val kotlinApiVersion = propertyAs<String>("mahout.kotlin.apiLevel")

version = propertyAs<String>("mahout.versionName")

val toolchain = propertyAs<String>("mahout.java.jvmToolchain")
val javaTarget = propertyAs<String>("mahout.java.jvmTarget")
val javaSource = propertyAs<String>("mahout.java.jvmSource")

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(toolchain.substringAfterLast('.')))
  }
  compilerOptions {
    jvmTarget.set(JvmTarget.fromTarget(javaTarget))
    freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    apiVersion.set(KotlinVersion.fromVersion(kotlinApiVersion))
  }
}

java {
  targetCompatibility = JavaVersion.toVersion(javaTarget)
  sourceCompatibility = JavaVersion.toVersion(javaSource)
}

tasks.withType(JavaCompile::class.java).configureEach {
  options.release.set(javaTarget.substringAfterLast('.').toInt())
}

testing {
  @Suppress("UnstableApiUsage")
  suites.withType<JvmTestSuite>().configureEach {
    useJUnitJupiter(libs.versions.jUnit5)
  }
}
