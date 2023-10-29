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

import com.rickbusarow.kgx.javaExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  dependencies {
    classpath(libs.kotlin.gradle.plugin)
    classpath(libs.vanniktech.publish)
    classpath(libs.rickBusarow.kgx)
  }
}

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.ktlint)
  base
}

val kotlinApiVersion = project.property("KOTLIN_API").toString()

subprojects sub@{
  this@sub.layout.buildDirectory.set(this@sub.file("build-composite"))
}

allprojects ap@{
  version = property("VERSION_NAME") as String

  plugins.withType(KotlinBasePlugin::class.java).configureEach {

    val jdk = project.property("JDK_BUILD_LOGIC").toString()
    val target = property("JVM_TARGET_BUILD_LOGIC").toString()

    extensions.configure(KotlinJvmProjectExtension::class.java) {
      jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(jdk))
      }
    }

    tasks.withType(KotlinCompile::class.java).configureEach {
      kotlinOptions {
        apiVersion = kotlinApiVersion
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        jvmTarget = target
      }
    }

    this@ap.javaExtension.sourceCompatibility = JavaVersion.toVersion(target)

    val targetInt = target.substringAfterLast('.').toInt()
    tasks.withType(JavaCompile::class.java).configureEach {
      options.release.set(targetInt)
    }
  }
  tasks.withType(Test::class.java).configureEach {
    useJUnitPlatform()
  }
}
