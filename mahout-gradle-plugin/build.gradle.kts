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
  packageName.set("com.rickbusarow.mahout")
  useKotlinOutput {
    internalVisibility = true
  }

  listOf<Triple<String, List<String>, (String) -> String>>(
    Triple("Versions", libsCatalog.versionAliases) { libsCatalog.version(it) },
    Triple("PluginIds", libsCatalog.pluginAliases) { libsCatalog.pluginId(it) },
    Triple("Libs", libsCatalog.libraryAliases) { libsCatalog.library(it).get().toString() },
    Triple("Modules", libsCatalog.libraryAliases) {
      libsCatalog.library(it).get().module.toString()
    }
  )
    .forEach { (className, aliases, value) ->
      forClass(packageName = "com.rickbusarow.mahout.deps", className = className) {
        for (alias in aliases) {
          buildConfigField(alias.replace('.', '-'), value(alias))
        }
      }
    }
}

kotlin {
  explicitApi()
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

  api(project(":mahout-api"))
  api(project(":mahout-core"))

  compileOnly(gradleApi())

  compileOnly(project(":mahout-settings-annotations"))

  gradleTestImplementation(libs.junit.jupiter)
  gradleTestImplementation(libs.junit.jupiter.api)
  gradleTestImplementation(libs.junit.jupiter.engine)
  gradleTestImplementation(libs.junit.jupiter.params)
  gradleTestImplementation(libs.kase)
  gradleTestImplementation(libs.kase.gradle)
  gradleTestImplementation(libs.kase.gradle.dsl)
  gradleTestImplementation(libs.kotest.assertions.api)
  gradleTestImplementation(libs.kotest.assertions.core.jvm)
  gradleTestImplementation(libs.kotest.assertions.shared)

  implementation(libs.benManes.versions)
  implementation(libs.benManes.versions)
  implementation(libs.breadmoirai.github.release)
  implementation(libs.breadmoirai.github.release)
  implementation(libs.detekt.gradle)
  implementation(libs.detekt.gradle)
  implementation(libs.diffplug.spotless)
  implementation(libs.diffplug.spotless)
  implementation(libs.dokka.core)
  implementation(libs.dokka.core)
  implementation(libs.dokka.gradle)
  implementation(libs.dokka.gradle)
  implementation(libs.dokka.versioning)
  implementation(libs.dokka.versioning)
  implementation(libs.dokkatoo.plugin)
  implementation(libs.dokkatoo.plugin)
  implementation(libs.drewHamilton.poko.gradle.plugin)
  implementation(libs.drewHamilton.poko.gradle.plugin)
  implementation(libs.dropbox.dependencyGuard)
  implementation(libs.dropbox.dependencyGuard)
  implementation(libs.ec4j.core)
  implementation(libs.ec4j.core)
  implementation(libs.johnrengelman.shadowJar)
  implementation(libs.johnrengelman.shadowJar)
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.kotlin.gradle.plugin.api)
  implementation(libs.kotlin.gradle.plugin.api)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlinx.binaryCompatibility)
  implementation(libs.kotlinx.binaryCompatibility)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.picnic)
  implementation(libs.rickBusarow.doks)
  implementation(libs.rickBusarow.doks)
  implementation(libs.rickBusarow.kgx)
  implementation(libs.rickBusarow.kgx)
  implementation(libs.rickBusarow.ktlint)
  implementation(libs.rickBusarow.ktlint)
  implementation(libs.rickBusarow.moduleCheck.gradle.plugin) {
    exclude(group = "org.jetbrains.kotlin")
  }
  implementation(libs.square.kotlinPoet)
  implementation(libs.vanniktech.publish.nexus)
  implementation(libs.vanniktech.publish.plugin)

  ksp(project(":mahout-settings-generator"))

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.junit.jupiter.engine)
  testImplementation(libs.junit.jupiter.params)
  testImplementation(libs.kase)
  testImplementation(libs.kotest.assertions.api)
  testImplementation(libs.kotest.assertions.core.jvm)
  testImplementation(libs.kotest.assertions.shared)
}

gradlePlugin {

  plugins {

    plugin(
      name = "composite",
      implementationClass = "com.rickbusarow.mahout.composite.CompositePlugin",
      description = "Propagates unqualified task requests from the root build to all included builds",
      additionalTags = emptyList()
    )

    plugin(
      name = "curator",
      implementationClass = "com.rickbusarow.mahout.curator.CuratorPlugin",
      description = "Verifies the consistency of a project's published artifacts",
      additionalTags = emptyList()
    )

    plugin(
      name = "gradle-test",
      implementationClass = "com.rickbusarow.mahout.conventions.GradleTestsPlugin",
      description = "Configures a source set for Gradle integration tests",
      additionalTags = listOf("testing", "gradle-plugin", "plugin", "kotlin-jvm")
    )

    plugin(
      name = "java-gradle-plugin",
      implementationClass = "com.rickbusarow.mahout.GradlePluginModulePlugin",
      description = "Convention plugin for a java-gradle-plugin project",
      additionalTags = listOf("plugin", "java", "jvm", "kotlin-jvm")
    )

    plugin(
      name = "kotlin-jvm-module",
      implementationClass = "com.rickbusarow.mahout.KotlinJvmModulePlugin",
      description = "Convention plugin for a Kotlin JVM project",
      additionalTags = listOf("java", "jvm", "kotlin-jvm")
    )

    plugin(
      name = "kotlin-multiplatform-module",
      implementationClass = "com.rickbusarow.mahout.KotlinMultiplatformModulePlugin",
      description = "Convention plugin for a Kotlin Multiplatform project",
      additionalTags = listOf("multiplatform", "kotlin-multiplatform")
    )

    plugin(
      name = "root",
      implementationClass = "com.rickbusarow.mahout.RootPlugin",
      description = "Convention plugin for the root project of a multi-module build",
      additionalTags = emptyList()
    )

    fun convention(simpleId: String, implementationClass: String) {
      plugin(
        name = simpleId,
        implementationClass = "com.rickbusarow.mahout.$implementationClass",
        description = "                ",
        additionalTags = emptyList()
      )
    }

    convention("convention.ben-manes", "conventions.BenManesVersionsPlugin")
    convention("convention.clean", "conventions.CleanPlugin")
    convention("convention.dependency-guard", "conventions.DependencyGuardConventionPlugin")
    convention("convention.detekt", "conventions.DetektConventionPlugin")
    convention("convention.dokka-versioning", "conventions.DokkaVersionArchivePlugin")
    convention("convention.dokkatoo", "dokka.DokkatooConventionPlugin")
    convention("convention.fix", "conventions.FixPlugin")
    convention("convention.feature-variants", "variants.FeatureVariantsPlugin")
    convention("convention.github-release", "conventions.GitHubReleasePlugin")
    convention("convention.kotlin-jvm", "conventions.KotlinJvmConventionPlugin")
    convention("convention.ktlint", "conventions.KtLintConventionPlugin")
    convention("convention.spotless", "conventions.SpotlessConventionPlugin")
    convention("convention.test", "conventions.TestConventionPlugin")
  }
}

fun NamedDomainObjectContainer<PluginDeclaration>.plugin(
  name: String,
  implementationClass: String,
  description: String,
  additionalTags: List<String>
): NamedDomainObjectProvider<PluginDeclaration> = register(name) {
  val declaration = this@register
  declaration.id = "com.rickbusarow.mahout.$name"
  declaration.implementationClass = implementationClass
  declaration.description = description
  @Suppress("UnstableApiUsage")
  declaration.tags.addAll("convention-plugin", "kotlin", *additionalTags.toTypedArray())
}

if (rootProject.name == "mahout") {

  apply(plugin = "com.rickbusarow.mahout.java-gradle-plugin")
  apply(plugin = libs.plugins.vanniktech.publish.base.get().pluginId)

  gradlePlugin {

    val gitHubUrl: String = project.property("mahout.publishing.pom.url") as String
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
        publication.artifactId = "mahout-gradle-plugin"
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
