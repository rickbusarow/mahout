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

import com.rickbusarow.kgx.buildDir
import com.rickbusarow.kgx.java
import com.rickbusarow.kgx.withBuildInitPlugin
import com.rickbusarow.kgx.withKotlinJvmPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

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
  id("com.rickbusarow.mahout.jvm-module") apply false
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
  tasks.addTasksToIdeSync(
    ":mahout-gradle-plugin:generateBuildConfig",
    ":mahout-gradle-plugin:kspKotlin"
  )
}

subprojects sub@{
  val sub = this@sub
  sub.layout.buildDirectory.set(sub.file("build/root"))

  sub.plugins.apply("idea")

  sub.extensions.configure(IdeaModel::class) {
    module {
      generatedSourceDirs.add(sub.file("build"))
      excludeDirs = excludeDirs + sub.file("build")
    }
  }

  sub.layout.buildDirectory.set(sub.file("build/main"))

  sub.tasks.withType(Test::class).configureEach {
    systemProperty("kase.baseWorkingDir", buildDir().resolve("kase"))
  }

  if (!sub.name.startsWith("mahout-settings-") && sub.name != "mahout-api") {
    sub.plugins.withKotlinJvmPlugin {
      (sub.kotlinExtension as KotlinJvmProjectExtension)
        .compilerOptions
        .optIn
        .add("com.rickbusarow.mahout.core.InternalMahoutApi")
    }
  }
}

allprojects ap@{

  // version = VERSION_NAME

  this@ap.plugins.withBuildInitPlugin {
    apply(plugin = libs.plugins.rickBusarow.ktlint.get().pluginId)

    dependencies {
      "ktlint"(rootProject.libs.rickBusarow.ktrules)
    }
  }
}
