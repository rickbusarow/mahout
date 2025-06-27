/*
 * Copyright (C) 2025 Rick Busarow
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

package com.rickbusarow.mahout.publishing

import com.rickbusarow.kase.gradle.dsl.buildFile
import com.rickbusarow.kase.kase
import com.rickbusarow.kase.stdlib.cartesianProduct
import com.rickbusarow.mahout.MahoutGradleTest
import com.rickbusarow.mahout.curator.ArtifactConfig
import com.rickbusarow.mahout.deps.PluginIds
import com.rickbusarow.mahout.deps.Versions
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import kotlinx.serialization.json.Json
import modulecheck.utils.mapToSet
import org.junit.jupiter.api.TestFactory

class MahoutPublishPluginTest : MahoutGradleTest {

  @TestFactory
  fun `checkVersionIsSnapshot passes for a snapshot version`() =
    listOf(
      listOf("1.2.3", "1", "0.1.0", "8.9", "2222.5555"),
      listOf(null, "-RC-1", "-beta-2", "-alpha-3")
    )
      .cartesianProduct()
      .mapToSet { (a1, a2) ->
        val version = a2?.let { "$a1$a2-SNAPSHOT" } ?: "$a1-SNAPSHOT"
        kase(displayName = "version: $version", a1 = version)
      }
      .asContainers { (version) ->
        testFactory { _ ->

          rootProject {

            buildFile {
              plugins {
                id("com.rickbusarow.mahout.root")
              }
            }

            gradlePropertiesFile(
              """
              mahout.versionName=$version
              """.trimIndent()
            )
          }

          shouldSucceed("checkVersionIsSnapshot", withPluginClasspath = true)
        }
      }

  @TestFactory
  fun `checkVersionIsSnapshot fails for any non-snapshot version`() =
    listOf(
      listOf("1.2.3", "1", "0.1.0", "8.9", "2222.5555"),
      listOf(null, "-SNAPSHOTS", "-RC-1", "-beta-2", "-alpha-3")
    )
      .cartesianProduct()
      .mapToSet { (a1, a2) ->
        val version = a2?.let { "$a1$a2" } ?: a1
        kase(displayName = "version: $version", a1 = version)
      }
      .asContainers { (version) ->
        testFactory {
          rootProject {

            buildFile {
              plugins {
                id("com.rickbusarow.mahout.root")
              }
            }

            gradlePropertiesFile(
              """
              mahout.versionName=$version
              """.trimIndent()
            )
          }

          shouldFail("checkVersionIsSnapshot", withPluginClasspath = true)
        }
      }

  @TestFactory
  fun `published plugins do not overwrite other plugin descriptions or pluginMaven`() = testFactory { versions ->
    rootProject {

      buildFile {
        plugins {
          kotlin("jvm", version = versions.kotlinVersion)
          id(PluginIds.`plugin-publish`, version = Versions.`gradle-plugin-publish`)
          id(PluginIds.`vanniktech-publish-base`, version = Versions.`vanniktech-publish`)
          id("com.rickbusarow.mahout.java-gradle-plugin", version = mahoutVersion)
        }
        raw(
          """
            mahout {
              versionName = "1.0.0"

              publishing {
                pluginMaven(
                  groupId = "com.test",
                  artifactId = "maven-test",
                  pomDescription = "plugin maven description"
                )

                publishPlugin(
                  gradlePlugin.plugins.register("a") {
                    id = "com.test.a"
                    implementationClass = "com.test.PluginA"
                    description = "description a"
                  }
                )

                publishPlugin(
                  gradlePlugin.plugins.register("b") {
                    id = "com.test.b"
                    implementationClass = "com.test.PluginB"
                    description = "description b"
                  }
                )
              }
            }

          """.trimIndent()
        )
      }
      kotlinFile(
        "src/main/kotlin/com/test/plugins.kt",
        """
            package com.test

            import org.gradle.api.Plugin
            import org.gradle.api.Project

            class PluginA : Plugin<Project> {
              override fun apply(target: Project) {
                // Plugin A implementation
              }
            }

            class PluginB : Plugin<Project> {
              override fun apply(target: Project) {
                // Plugin B implementation
              }
            }
        """.trimIndent()
      )
    }

    shouldSucceed("artifactsDump") {
      val artifacts = workingDir
        .resolve("artifacts.json")
        .readText()
        .let { Json.Default.decodeFromString<List<ArtifactConfig>>(it) }

      artifacts shouldContainExactlyInAnyOrder listOf(
        ArtifactConfig(
          gradlePath = ":",
          group = "com.test.a",
          artifactId = "com.test.a.gradle.plugin",
          description = "description a",
          javaVersion = "11",
          packaging = "pom",
          publicationName = "aPluginMarkerMaven"
        ),
        ArtifactConfig(
          gradlePath = ":",
          group = "com.test.b",
          artifactId = "com.test.b.gradle.plugin",
          description = "description b",
          javaVersion = "11",
          packaging = "pom",
          publicationName = "bPluginMarkerMaven"
        ),
        ArtifactConfig(
          gradlePath = ":",
          group = "com.test",
          artifactId = "maven-test",
          description = "plugin maven description",
          javaVersion = "11",
          packaging = "jar",
          publicationName = "pluginMaven"
        )
      )
    }
  }
}
