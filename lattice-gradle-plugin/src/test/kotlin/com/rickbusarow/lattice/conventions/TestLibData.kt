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

package com.rickbusarow.lattice.conventions

import com.rickbusarow.lattice.libs.CatalogSection
import com.rickbusarow.lattice.libs.CatalogSection.AliasToValue

internal object TestLibData {
  val versions = CatalogSection(
    listOf(
      AliasToValue("benManes.versions", "0.50.0"),
      AliasToValue("breadmoirai.github.release", "2.5.2"),
      AliasToValue("buildconfig", "5.3.5"),
      AliasToValue("dependencyAnalysis", "1.29.0"),
      AliasToValue("detekt", "1.23.4"),
      AliasToValue("diffplug.spotless", "6.24.0"),
      AliasToValue("dokka", "1.9.10"),
      AliasToValue("dokkatoo", "2.0.0"),
      AliasToValue("drewHamilton.poko", "0.15.2"),
      AliasToValue("dropbox.dependencyGuard", "0.4.3"),
      AliasToValue("ec4j", "0.3.0"),
      AliasToValue("google.auto.service", "1.1.1"),
      AliasToValue("gradle.plugin.publish", "1.1.0"),
      AliasToValue("gradlex.buildParameters", "1.4.3"),
      AliasToValue("integration.test", "1.4.5"),
      AliasToValue("jUnit", "5.10.1"),
      AliasToValue("johnrengelman.shadow", "8.1.1"),
      AliasToValue("koin", "3.5.1"),
      AliasToValue("kotest", "5.8.0"),
      AliasToValue("kotlin", "1.9.22"),
      AliasToValue("kotlinx.binaryCompatibility", "0.13.2"),
      AliasToValue("kotlinx.coroutines", "1.7.3"),
      AliasToValue("kotlinx.serialization", "1.6.2"),
      AliasToValue("ksp", "1.9.22-1.0.16"),
      AliasToValue("ktlint.lib", "0.50.0"),
      AliasToValue("picnic", "0.7.0"),
      AliasToValue("prettier", "2.8.4"),
      AliasToValue("prettier.plugin.sh", "0.12.8"),
      AliasToValue("rickBusarow.doks", "0.1.4"),
      AliasToValue("rickBusarow.kase", "0.4.0"),
      AliasToValue("rickBusarow.kgx", "0.1.10"),
      AliasToValue("rickBusarow.ktlint", "0.2.2"),
      AliasToValue("rickBusarow.ktrules", "1.3.1"),
      AliasToValue("rickBusarow.lattice", "0.1.0-SNAPSHOT"),
      AliasToValue("rickBusarow.moduleCheck", "0.12.5"),
      AliasToValue("square.kotlinPoet", "1.16.0"),
      AliasToValue("square.okio", "3.7.0"),
      AliasToValue("vanniktech.publish", "0.27.0")
    )
  )

  val plugins = CatalogSection(
    listOf(
      AliasToValue("adamko.dokkatoo", "dev.adamko.dokkatoo"),
      AliasToValue("adamko.dokkatoo.html", "dev.adamko.dokkatoo-html"),
      AliasToValue("autonomousapps.dependencyAnalysis", "com.autonomousapps.dependency-analysis"),
      AliasToValue("breadmoirai.github.release", "com.github.breadmoirai.github-release"),
      AliasToValue("buildconfig", "com.github.gmazzo.buildconfig"),
      AliasToValue("drewHamilton.poko", "dev.drewhamilton.poko"),
      AliasToValue("dropbox.dependency.guard", "com.dropbox.dependency-guard"),
      AliasToValue("gmazzo.buildconfig", "com.github.gmazzo.buildconfig"),
      AliasToValue("gradlex.buildParameters", "org.gradlex.build-parameters"),
      AliasToValue("integration.test", "com.coditory.integration-test"),
      AliasToValue("johnrengelman.shadow", "com.github.johnrengelman.shadow"),
      AliasToValue("kotlin.android", "org.jetbrains.kotlin.android"),
      AliasToValue("kotlin.js", "org.jetbrains.kotlin.js"),
      AliasToValue("kotlin.jvm", "org.jetbrains.kotlin.jvm"),
      AliasToValue("kotlin.multiplatform", "org.jetbrains.kotlin.multiplatform"),
      AliasToValue("kotlin.serialization", "org.jetbrains.kotlin.plugin.serialization"),
      AliasToValue(
        "kotlinx.binaryCompatibility",
        "org.jetbrains.kotlinx.binary-compatibility-validator"
      ),
      AliasToValue("ksp", "com.google.devtools.ksp"),
      AliasToValue("lattice.composite", "com.rickbusarow.lattice.composite"),
      AliasToValue("lattice.java.gradle.plugin", "com.rickbusarow.lattice.java-gradle-plugin"),
      AliasToValue("lattice.kotlin.jvm", "com.rickbusarow.lattice.kotlin-jvm"),
      AliasToValue("lattice.kotlin.multiplatform", "com.rickbusarow.lattice.kotlin-multiplatform"),
      AliasToValue("lattice.root", "com.rickbusarow.lattice.root"),
      AliasToValue("plugin.publish", "com.gradle.plugin-publish"),
      AliasToValue("rickBusarow.doks", "com.rickbusarow.doks"),
      AliasToValue("rickBusarow.ktlint", "com.rickbusarow.ktlint"),
      AliasToValue("rickBusarow.moduleCheck", "com.rickbusarow.module-check"),
      AliasToValue("vanniktech.publish", "com.vanniktech.maven.publish"),
      AliasToValue("vanniktech.publish.base", "com.vanniktech.maven.publish.base")
    )
  )

  val libraries = CatalogSection(
    listOf(
      AliasToValue("benManes.versions", "com.github.ben-manes:gradle-versions-plugin:0.50.0"),
      AliasToValue(
        "breadmoirai.github.release",
        "com.rickbusarow.githubreleasepluginfork:github-release:2.5.2"
      ),
      AliasToValue("buildconfig", "com.github.gmazzo:gradle-buildconfig-plugin:5.3.5"),
      AliasToValue("detekt.api", "io.gitlab.arturbosch.detekt:detekt-api:1.23.4"),
      AliasToValue("detekt.cli", "io.gitlab.arturbosch.detekt:detekt-cli:1.23.4"),
      AliasToValue("detekt.gradle", "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.4"),
      AliasToValue(
        "detekt.rules.libraries",
        "io.gitlab.arturbosch.detekt:detekt-rules-libraries:1.23.4"
      ),
      AliasToValue(
        "detekt.rules.ruleauthors",
        "io.gitlab.arturbosch.detekt:detekt-rules-ruleauthors:1.23.4"
      ),
      AliasToValue("detekt.test", "io.gitlab.arturbosch.detekt:detekt-test:1.23.4"),
      AliasToValue("detekt.test.utils", "io.gitlab.arturbosch.detekt:detekt-test-utils:1.23.4"),
      AliasToValue("diffplug.spotless", "com.diffplug.spotless:spotless-plugin-gradle:6.24.0"),
      AliasToValue("dokka.all.modules", "org.jetbrains.dokka:all-modules-page-plugin:1.9.10"),
      AliasToValue("dokka.core", "org.jetbrains.dokka:dokka-core:1.9.10"),
      AliasToValue("dokka.gradle", "org.jetbrains.dokka:dokka-gradle-plugin:1.9.10"),
      AliasToValue("dokka.versioning", "org.jetbrains.dokka:versioning-plugin:1.9.10"),
      AliasToValue("dokkatoo.plugin", "dev.adamko.dokkatoo:dokkatoo-plugin:2.0.0"),
      AliasToValue(
        "drewHamilton.poko.annotations",
        "dev.drewhamilton.poko:poko-annotations:0.15.2"
      ),
      AliasToValue(
        "drewHamilton.poko.compiler",
        "dev.drewhamilton.poko:poko-compiler-plugin:0.15.2"
      ),
      AliasToValue(
        "drewHamilton.poko.gradle.plugin",
        "dev.drewhamilton.poko:poko-gradle-plugin:0.15.2"
      ),
      AliasToValue(
        "dropbox.dependencyGuard",
        "com.dropbox.dependency-guard:dependency-guard:0.4.3"
      ),
      AliasToValue("ec4j.core", "org.ec4j.core:ec4j-core:0.3.0"),
      AliasToValue(
        "google.auto.service.annotations",
        "com.google.auto.service:auto-service-annotations:1.1.1"
      ),
      AliasToValue("gradlex.buildParameters", "org.gradlex:build-parameters:1.4.3"),
      AliasToValue("integration.test", "com.coditory.gradle:integration-test-plugin:1.4.5"),
      AliasToValue("johnrengelman.shadowJar", "com.github.johnrengelman:shadow:8.1.1"),
      AliasToValue("junit.engine", "org.junit.jupiter:junit-jupiter-engine:5.10.1"),
      AliasToValue("junit.jupiter", "org.junit.jupiter:junit-jupiter:5.10.1"),
      AliasToValue("junit.jupiter.api", "org.junit.jupiter:junit-jupiter-api:5.10.1"),
      AliasToValue("junit.params", "org.junit.jupiter:junit-jupiter-params:5.10.1"),
      AliasToValue("kase", "com.rickbusarow.kase:kase:0.4.0"),
      AliasToValue("kase.gradle", "com.rickbusarow.kase:kase-gradle:0.4.0"),
      AliasToValue("kase.gradle.dsl", "com.rickbusarow.kase:kase-gradle-dsl:0.4.0"),
      AliasToValue("koin", "io.insert-koin:koin-core:3.5.1"),
      AliasToValue("kotest.assertions.api", "io.kotest:kotest-assertions-api:5.8.0"),
      AliasToValue("kotest.assertions.core.jvm", "io.kotest:kotest-assertions-core-jvm:5.8.0"),
      AliasToValue("kotest.assertions.shared", "io.kotest:kotest-assertions-shared:5.8.0"),
      AliasToValue("kotest.common", "io.kotest:kotest-common:5.8.0"),
      AliasToValue("kotest.extensions", "io.kotest:kotest-extensions:5.8.0"),
      AliasToValue("kotest.property.jvm", "io.kotest:kotest-property-jvm:5.8.0"),
      AliasToValue("kotest.runner.junit5.jvm", "io.kotest:kotest-runner-junit5-jvm:5.8.0"),
      AliasToValue(
        "kotlin.annotation.processing",
        "org.jetbrains.kotlin:kotlin-annotation-processing-embeddable:1.9.22"
      ),
      AliasToValue("kotlin.bom", "org.jetbrains.kotlin:kotlin-bom:1.9.22"),
      AliasToValue("kotlin.compiler", "org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.22"),
      AliasToValue("kotlin.gradle.plugin", "org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22"),
      AliasToValue(
        "kotlin.gradle.plugin.api",
        "org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.9.22"
      ),
      AliasToValue("kotlin.reflect", "org.jetbrains.kotlin:kotlin-reflect:1.9.22"),
      AliasToValue(
        "kotlin.sam.with.receiver",
        "org.jetbrains.kotlin:kotlin-sam-with-receiver:1.9.22"
      ),
      AliasToValue("kotlin.stdlib.common", "org.jetbrains.kotlin:kotlin-stdlib-common:1.9.22"),
      AliasToValue("kotlin.stdlib.jdk7", "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.22"),
      AliasToValue("kotlin.stdlib.jdk8", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22"),
      AliasToValue("kotlin.test", "org.jetbrains.kotlin:kotlin-test:1.9.22"),
      AliasToValue(
        "kotlinx.binaryCompatibility",
        "org.jetbrains.kotlinx:binary-compatibility-validator:0.13.2"
      ),
      AliasToValue(
        "kotlinx.coroutines.android",
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
      ),
      AliasToValue(
        "kotlinx.coroutines.core",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3"
      ),
      AliasToValue(
        "kotlinx.coroutines.coreCommon",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.7.3"
      ),
      AliasToValue(
        "kotlinx.coroutines.debug",
        "org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.7.3"
      ),
      AliasToValue(
        "kotlinx.coroutines.jdk8",
        "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3"
      ),
      AliasToValue(
        "kotlinx.coroutines.jvm",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3"
      ),
      AliasToValue(
        "kotlinx.coroutines.play.services",
        "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3"
      ),
      AliasToValue(
        "kotlinx.coroutines.test",
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"
      ),
      AliasToValue(
        "kotlinx.serialization.core",
        "org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2"
      ),
      AliasToValue(
        "kotlinx.serialization.json",
        "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2"
      ),
      AliasToValue("ksp.api", "com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.16"),
      AliasToValue(
        "ksp.gradle.plugin",
        "com.google.devtools.ksp:symbol-processing-gradle-plugin:1.9.22-1.0.16"
      ),
      AliasToValue("picnic", "com.jakewharton.picnic:picnic:0.7.0"),
      AliasToValue("rickBusarow.doks", "com.rickbusarow.doks:doks-gradle-plugin:0.1.4"),
      AliasToValue("rickBusarow.kgx", "com.rickbusarow.kgx:kotlin-gradle-extensions:0.1.10"),
      AliasToValue("rickBusarow.kgx.names", "com.rickbusarow.kgx:names:0.1.10"),
      AliasToValue("rickBusarow.ktlint", "com.rickbusarow.ktlint:ktlint-gradle-plugin:0.2.2"),
      AliasToValue("rickBusarow.ktrules", "com.rickbusarow.ktrules:ktrules:1.3.1"),
      AliasToValue(
        "rickBusarow.moduleCheck.gradle.plugin",
        "com.rickbusarow.modulecheck:plugin:0.12.5"
      ),
      AliasToValue("square.kotlinPoet", "com.squareup:kotlinpoet:1.16.0"),
      AliasToValue("square.kotlinPoet.ksp", "com.squareup:kotlinpoet-ksp:1.16.0"),
      AliasToValue("square.okio", "com.squareup.okio:okio:3.7.0"),
      AliasToValue("vanniktech.publish.nexus", "com.vanniktech:nexus:0.27.0"),
      AliasToValue(
        "vanniktech.publish.plugin",
        "com.vanniktech:gradle-maven-publish-plugin:0.27.0"
      ),
      AliasToValue("zacSweers.auto.service.ksp", "dev.zacsweers.autoservice:auto-service-ksp:1.1.0")
    )
  )

  val bundles = CatalogSection(
    listOf(
      AliasToValue(
        "jUnit",
        "property(org.gradle.api.artifacts.ExternalModuleDependencyBundle, map(org.gradle.api.artifacts.ExternalModuleDependencyBundle map(valueof(DependencyBundleValueSource)) check-type()))"
      ),
      AliasToValue(
        "kotest",
        "property(org.gradle.api.artifacts.ExternalModuleDependencyBundle, map(org.gradle.api.artifacts.ExternalModuleDependencyBundle map(valueof(DependencyBundleValueSource)) check-type()))"
      )
    )
  )
}
