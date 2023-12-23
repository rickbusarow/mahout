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

@file:Suppress("VariableNaming")

import com.rickbusarow.kgx.extras
import com.rickbusarow.kgx.library
import com.rickbusarow.kgx.libsCatalog
import com.rickbusarow.kgx.pluginId
import com.rickbusarow.kgx.version
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  dependencies {
    classpath(libs.rickBusarow.kgx)
  }
}

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.drewHamilton.poko)
  alias(libs.plugins.plugin.publish)
  alias(libs.plugins.vanniktech.publish.base) apply false
  alias(libs.plugins.buildconfig)
  idea
}

buildConfig {
  packageName.set("com.rickbusarow.lattice")
  useKotlinOutput {
    internalVisibility = true
  }

  forClass("com.rickbusarow.lattice.deps", "Versions") {

    for (alias in libsCatalog.versionAliases) {
      buildConfigField(alias.replace('.', '-'), libsCatalog.version(alias))
    }
  }
  forClass("com.rickbusarow.lattice.deps", "PluginIds") {

    for (alias in libsCatalog.pluginAliases) {
      buildConfigField(alias.replace('.', '-'), libsCatalog.pluginId(alias))
    }
  }
  forClass("com.rickbusarow.lattice.deps", "Libs") {

    for (alias in libsCatalog.libraryAliases) {
      buildConfigField(alias.replace('.', '-'), libsCatalog.library(alias).get().toString())
    }
  }
  forClass("com.rickbusarow.lattice.deps", "Modules") {

    for (alias in libsCatalog.libraryAliases) {
      buildConfigField(alias.replace('.', '-'), libsCatalog.library(alias).get().module.toString())
    }
  }
}

val gradleTest by sourceSets.registering {
  val ss = this@registering

  val main by sourceSets.getting

  gradlePlugin.testSourceSets(ss)

  ss.compileClasspath += main.output
  ss.runtimeClasspath += main.output

  configurations.named(ss.implementationConfigurationName) {
    extendsFrom(configurations.getByName(main.implementationConfigurationName))
  }
  configurations.named(ss.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.getByName(main.runtimeOnlyConfigurationName))
  }
  configurations.named(ss.compileOnlyConfigurationName) {
    extendsFrom(configurations.getByName(main.compileOnlyConfigurationName))
  }
}

tasks.register("gradleTest", Test::class) {
  useJUnitPlatform()

  val javaSourceSet = gradleTest.get()

  testClassesDirs = javaSourceSet.output.classesDirs
  classpath = javaSourceSet.runtimeClasspath
  inputs.files(javaSourceSet.allSource)
}

tasks.named("check") { dependsOn("gradleTest") }

idea {
  module {
    testSources.from(gradleTest.map { it.allSource.srcDirs })
  }
}

val gradleTestImplementation: Configuration by configurations.getting

dependencies {

  compileOnly(gradleApi())

  compileOnly(project(":lattice-settings-annotations"))

  gradleTestImplementation(libs.junit.engine)
  gradleTestImplementation(libs.junit.jupiter)
  gradleTestImplementation(libs.junit.jupiter.api)
  gradleTestImplementation(libs.junit.params)
  gradleTestImplementation(libs.kase)
  gradleTestImplementation(libs.kase.gradle)
  gradleTestImplementation(libs.kase.gradle.dsl)
  gradleTestImplementation(libs.kotest.assertions.api)
  gradleTestImplementation(libs.kotest.assertions.core.jvm)
  gradleTestImplementation(libs.kotest.assertions.shared)

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
  implementation(libs.square.kotlinPoet)
  implementation(libs.vanniktech.publish.nexus)
  implementation(libs.vanniktech.publish.plugin)

  ksp(project(":lattice-settings-generator"))

  testImplementation(libs.junit.engine)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.params)
  testImplementation(libs.kase)
  testImplementation(libs.kotest.assertions.api)
  testImplementation(libs.kotest.assertions.core.jvm)
  testImplementation(libs.kotest.assertions.shared)
}

fun PluginDeclaration.tags(vararg v: String) {
  @Suppress("UnstableApiUsage")
  tags.set(v.toList())
}

gradlePlugin {

  plugins {

    register("root") {
      id = "com.rickbusarow.lattice.root"
      implementationClass = "com.rickbusarow.lattice.RootPlugin"
      description = "Convention plugin for the root project of a multi-module build"
      tags("convention-plugin", "kotlin", "java", "jvm", "kotlin-jvm")
    }
    register("java-gradle-plugin") {
      id = "com.rickbusarow.lattice.java-gradle-plugin"
      implementationClass = "com.rickbusarow.lattice.GradlePluginModulePlugin"
      description = "Convention plugin for a java-gradle-plugin project"
      tags("convention-plugin", "kotlin", "plugin", "java", "jvm", "kotlin-jvm")
    }
    register("jvm") {
      id = "com.rickbusarow.lattice.kotlin-jvm"
      implementationClass = "com.rickbusarow.lattice.KotlinJvmModulePlugin"
      description = "Convention plugin for a Kotlin JVM project"
      tags("convention-plugin", "kotlin", "java", "jvm", "kotlin-jvm")
    }
    register("kmp") {
      id = "com.rickbusarow.lattice.kotlin-multiplatform"
      implementationClass = "com.rickbusarow.lattice.KotlinMultiplatformModulePlugin"
      description = "Convention plugin for a Kotlin Multiplatform project"
      tags("convention-plugin", "kotlin", "multiplatform", "kotlin-multiplatform")
    }
  }
}

if (rootProject.name == "lattice") {

  apply(plugin = "com.rickbusarow.lattice.java-gradle-plugin")
  apply(plugin = libs.plugins.vanniktech.publish.base.get().pluginId)

  gradlePlugin {

    val gitHubUrl: String = project.property("lattice.publishing.pom.url") as String
    @Suppress("UnstableApiUsage")
    vcsUrl.set(gitHubUrl)
    @Suppress("UnstableApiUsage")
    website.set(gitHubUrl)
  }

  fun MavenPublication.isPluginMarker(): Boolean = name.endsWith("PluginMarkerMaven")
  fun Publication.isPluginMarker(): Boolean = (this as? MavenPublication)?.isPluginMarker() ?: false

  publishing {
    publications.withType<MavenPublication>().configureEach pub@{
      val publication = this@pub

      publication.groupId = project.group as String

      if (!publication.isPluginMarker()) {
        publication.artifactId = "lattice-gradle-plugin"
        publication.pom.description.set("Convention plugins for Gradle builds")
      }
    }

    repositories {
      maven {
        name = "buildM2"
        setUrl(layout.buildDirectory.dir("m2"))
      }
    }
  }

  tasks.register("publishToBuildM2") {
    group = "Publishing"
    dependsOn("publishAllPublicationsToBuildM2Repository")
  }
  tasks.register("publishToBuildM2NoDokka") {
    group = "Publishing"
    project.extras.set("skipDokka", true)
    dependsOn("publishAllPublicationsToBuildM2Repository")
  }
}

tasks.named("compileKotlin", KotlinCompile::class) {
  kotlinOptions {
    explicitApiMode.set(Strict)
  }
}
