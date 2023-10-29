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

rootProject.name = "antipasto"

pluginManagement {
  repositories {
    val allowMavenLocal = providers
      .gradleProperty("${rootProject.name}.allow-maven-local")
      .orNull.toBoolean()
    if (allowMavenLocal) {
      logger.lifecycle("${rootProject.name} -- allowing mavenLocal for plugins")
      mavenLocal()
    }
    gradlePluginPortal()
    mavenCentral()
    google()
  }

  includeBuild("build-logic")

  plugins {
    id("com.rickbusarow.antipasto.jvm-module") apply false
    id("com.rickbusarow.antipasto.root") apply false
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositories {
    val allowMavenLocal = providers
      .gradleProperty("${rootProject.name}.allow-maven-local")
      .orNull.toBoolean()

    if (allowMavenLocal) {
      logger.lifecycle("${rootProject.name} -- allowing mavenLocal for dependencies")
      mavenLocal()
    }
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

include(":module")

includeBuild("build-logic") {
  // dependencySubstitution {
  //   substitute(module("com.rickbusarow.antipasto:artifacts")).using(project(":artifacts"))
  //   substitute(module("com.rickbusarow.antipasto:conventions")).using(project(":conventions"))
  //   substitute(module("com.rickbusarow.antipasto:core")).using(project(":core"))
  //   substitute(module("com.rickbusarow.antipasto:module")).using(project(":module"))
  // }
}
