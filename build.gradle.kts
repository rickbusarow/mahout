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

import com.rickbusarow.kgx.withBuildInitPlugin
import org.gradle.kotlin.dsl.addTasksToStartParameter as addTasksToStartParameterDsl
import org.gradle.kotlin.dsl.mahoutProperties as mahoutPropertiesDsl

buildscript {
  dependencies {
    classpath(libs.rickBusarow.kgx)
  }
}

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.drewHamilton.poko) apply false
  alias(libs.plugins.rickBusarow.doks)
  alias(libs.plugins.rickBusarow.ktlint) apply false
  alias(libs.plugins.rickBusarow.moduleCheck)
  alias(libs.plugins.vanniktech.publish.base) apply false
  id("com.rickbusarow.mahout.kotlin-jvm-module") apply false
  id("com.rickbusarow.mahout.root")
}

moduleCheck {
  deleteUnused = true
  checks.sortDependencies = true
}

mahout {

  composite {
  }
  github {
  }
  dokka {
  }
  java {
  }
  addTasksToStartParameterDsl(
    ":mahout-gradle-plugin:generateBuildConfig",
    ":mahout-gradle-plugin:kspKotlin"
  )
}

allprojects ap@{

  version = mahoutPropertiesDsl.versionName.get()
  group = mahoutPropertiesDsl.group.get()

  this@ap.plugins.withBuildInitPlugin {
    apply(plugin = libs.plugins.rickBusarow.ktlint.get().pluginId)

    dependencies {
      "ktlint"(rootProject.libs.rickBusarow.ktrules)
    }
  }
}

subprojects sp@{
  this@sp.afterEvaluate {
    val id = "conventions.dogFood"
    check(this@sp.plugins.hasPlugin(id)) {
      "Every project must apply the '$id' plugin."
    }
  }
}
