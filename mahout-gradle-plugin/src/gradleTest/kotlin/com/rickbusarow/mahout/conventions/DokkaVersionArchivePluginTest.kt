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

package com.rickbusarow.mahout.conventions

import com.rickbusarow.kase.gradle.dsl.buildFile
import com.rickbusarow.kase.kase
import com.rickbusarow.kase.stdlib.div
import com.rickbusarow.mahout.MahoutGradleTest
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import org.junit.jupiter.api.TestFactory

class DokkaVersionArchivePluginTest : MahoutGradleTest {

  @TestFactory
  fun `version avoidance behavior`() = listOf(
    kase("a major version is archived", "1.0.0", true),
    kase("a minor version is archived", "1.1.0", true),
    kase("a patch version is archived", "1.0.1", true),
    kase("a beta version is archived", "1.0.1-beta01", true),
    kase("a snapshot version is not archived", "1.0.0-SNAPSHOT", false),
    kase("a beta snapshot version is not archived", "1.0.1-beta01-SNAPSHOT", false)
  ).asContainers { (version, shouldArchive) ->
    testFactory { _ ->

      rootProject {

        project("lib") {

          buildFile {
            plugins {
              id("com.rickbusarow.mahout.kotlin-jvm-module")
              id("com.rickbusarow.mahout.convention.dokka")
              kotlin("jvm")
            }
          }

          kotlinFile(
            "src/main/kotlin/com/example/Example.kt",
            """
            package com.example

            class Example
            """.trimIndent()
          )
        }

        settingsFileAsFile.appendText(
          """

          include("lib")
          """.trimIndent()
        )

        gradlePropertiesFile(
          """
            mahout.versionName=$version
          """.trimIndent()
        )
      }

      shouldSucceed("syncDokkaToArchive")

      val archiveZip = workingDir / "dokka-archive" / "$version.zip"

      if (shouldArchive) {
        archiveZip.shouldExist()
      } else {
        archiveZip.shouldNotExist()
      }
    }
  }

  @TestFactory
  fun `archived versions are included in dokka publications`() =
    testFactory { _ ->

      rootProject {

        project("lib") {

          buildFile {
            plugins {
              id("com.rickbusarow.mahout.kotlin-jvm-module")
              id("com.rickbusarow.mahout.convention.dokka")
              kotlin("jvm")
            }
          }

          kotlinFile(
            "src/main/kotlin/com/example/Example.kt",
            """
            package com.example

            class Example
            """.trimIndent()
          )
        }

        settingsFileAsFile.appendText(
          """

          include("lib")
          """.trimIndent()
        )

        gradlePropertiesFile("mahout.versionName=1.0.0")
      }

      shouldSucceed("syncDokkaToArchive")

      rootProject.gradlePropertiesFile("mahout.versionName=1.0.1")

      shouldSucceed("dokkaGeneratePublicationHtml")

      val olderDir = workingDir / "build/dokka/html/older/1.0.0"

      olderDir.shouldExist()

      shouldSucceed("checkJavadocJarIsNotVersioned")
    }

  @TestFactory
  fun `archived versions are not included in javadoc`() =
    testFactory { _ ->

      rootProject {

        project("lib") {

          buildFile {
            plugins {
              id("com.rickbusarow.mahout.kotlin-jvm-module")
              id("com.rickbusarow.mahout.convention.dokka")
              kotlin("jvm")
            }
          }

          kotlinFile(
            "src/main/kotlin/com/example/Example.kt",
            """
            package com.example

            class Example
            """.trimIndent()
          )
        }

        settingsFileAsFile.appendText(
          """

          include("lib")
          """.trimIndent()
        )

        gradlePropertiesFile("mahout.versionName=1.0.0")
      }

      shouldSucceed("checkJavadocJarIsNotVersioned")
    }
}
