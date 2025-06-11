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

import com.rickbusarow.kase.gradle.GradleTestVersions
import com.rickbusarow.kase.gradle.dsl.buildFile
import com.rickbusarow.kase.gradle.versions
import com.rickbusarow.kase.kase
import com.rickbusarow.kase.stdlib.cartesianProduct
import com.rickbusarow.mahout.MahoutGradleTest
import modulecheck.utils.mapToSet
import org.junit.jupiter.api.TestFactory

class MahoutPublishPluginTest : MahoutGradleTest {

  override val params: List<GradleTestVersions>
    get() = kaseMatrix.versions(GradleTestVersions)

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
}
