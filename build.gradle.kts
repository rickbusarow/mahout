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
  alias(libs.plugins.poko) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.ktlint) apply false
  alias(libs.plugins.doks)
  alias(libs.plugins.moduleCheck)
  id("com.rickbusarow.lattice.jvm-module") apply false
  id("com.rickbusarow.lattice.root")
}

moduleCheck {
  deleteUnused = true
  checks.sortDependencies = true
}

lattice {

  composite {
  }
}

val ktlintPluginId = libs.plugins.ktlint.get().pluginId

allprojects ap@{
  version = property("VERSION_NAME") as String

  val innerProject = this@ap

  apply(plugin = ktlintPluginId)

  dependencies {
    "ktlint"(rootProject.libs.rickBusarow.ktrules)
  }

  // if (innerProject != rootProject) {
  //   rootProject.tasks.named("ktlintCheck") {
  //     dependsOn(innerProject.tasks.named("ktlintCheck"))
  //   }
  //   rootProject.tasks.named("ktlintFormat") {
  //     dependsOn(innerProject.tasks.named("ktlintFormat"))
  //   }
  // }
}

tasks.named("clean") {
  dependsOn(gradle.includedBuild("build-logic").task(":clean"))
}
