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

import com.rickbusarow.kgx.extras
import com.rickbusarow.kgx.javaExtension
import com.rickbusarow.kgx.propertyAs
import com.rickbusarow.kgx.withKotlinJvmPlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

buildscript {
  dependencies {
    classpath(libs.kotlin.gradle.plugin)
    classpath(libs.vanniktech.publish.plugin)
    classpath(libs.rickBusarow.kgx)
  }
}

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.rickBusarow.ktlint)
  alias(libs.plugins.vanniktech.publish.base) apply false
  base
}

rootProject.file("../gradle.properties")
  .inputStream()
  .use { Properties().apply { load(it) } }
  .forEach { key, value ->
    extras.set(key.toString(), value.toString())
  }

val kotlinApiVersion = propertyAs<String>("mahout.kotlin.apiLevel")

subprojects sub@{
  val sub = this@sub
  sub.layout.buildDirectory.set(sub.file("build/composite"))

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

  version = propertyAs<String>("mahout.versionName")

  plugins.withType(KotlinBasePlugin::class.java).configureEach {

    val jdk = propertyAs<String>("mahout.java.jvmToolchain")
    val target = propertyAs<String>("mahout.java.jvmTarget")

    extensions.configure(KotlinJvmProjectExtension::class.java) {
      jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(jdk.substringAfterLast('.')))
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
