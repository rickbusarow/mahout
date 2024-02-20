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
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
}

if (rootProject.name == "mahout") {
  apply(plugin = "com.rickbusarow.mahout.jvm-module")
}

dependencies {
  compileOnly(libs.google.auto.service.annotations)

  implementation(libs.ksp.api)
  implementation(libs.square.kotlinPoet)
  implementation(libs.square.kotlinPoet.ksp)

  implementation(project(":mahout-settings-annotations"))

  ksp(libs.zacSweers.auto.service.ksp)
}
