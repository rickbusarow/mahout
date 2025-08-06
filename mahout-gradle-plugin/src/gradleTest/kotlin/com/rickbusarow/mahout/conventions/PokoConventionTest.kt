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
import com.rickbusarow.mahout.MahoutGradleTest
import com.rickbusarow.mahout.deps.PluginIds
import com.rickbusarow.mahout.deps.Versions
import io.kotest.matchers.string.shouldInclude
import io.kotest.matchers.string.shouldNotInclude
import org.junit.jupiter.api.TestFactory

class PokoConventionTest : MahoutGradleTest {

  @TestFactory
  fun `the poko convention removes the annotations dependency from implementation`() =
    testFactory { _ ->

      rootProject {

        project("lib") {

          buildFile {
            plugins {
              id("com.rickbusarow.mahout.kotlin-jvm-module")
              kotlin("jvm")
              id(
                PluginIds.`drewHamilton-poko`,
                version = Versions.`drewHamilton-poko`,
                apply = false
              )
            }

            raw(
              """
              mahout {
                poko()
              }
              """.trimIndent()
            )
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
      }

      shouldSucceed("compileKotlin")

      shouldSucceed(":lib:dependencies", "--configuration", "compileOnly") {
        output shouldInclude "dev.drewhamilton.poko:poko-annotations:${Versions.`drewHamilton-poko`}"
      }

      shouldSucceed(":lib:dependencies", "--configuration", "implementation") {
        output shouldNotInclude "dev.drewhamilton.poko:poko-annotations:${Versions.`drewHamilton-poko`}"
      }
    }
}
