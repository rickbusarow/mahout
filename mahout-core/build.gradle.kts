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

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.drewHamilton.poko)
  alias(libs.plugins.buildconfig)
  id("com.rickbusarow.mahout.kotlin-jvm-module")
}

mahout {
  publishing {
    publishMaven(
      artifactId = "mahout-core",
      pomDescription = "Base implementation details for Mahout",
      name = "Mahout Core"
    )
  }
}

kotlin {
  compilerOptions {
    optIn.add("com.rickbusarow.mahout.core.InternalMahoutApi")
    explicitApi()
  }
}

dependencies {

  compileOnly(gradleApi())

  implementation(libs.rickBusarow.kgx)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.jupiter.engine)
  testImplementation(libs.junit.jupiter.params)
  testImplementation(libs.kase)
  testImplementation(libs.kotest.assertions.api)
  testImplementation(libs.kotest.assertions.core.jvm)
  testImplementation(libs.kotest.assertions.shared)
}
