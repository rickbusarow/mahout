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

package com.rickbusarow.lattice.config

import com.rickbusarow.kase.gradle.GradleTestVersions
import com.rickbusarow.lattice.LatticeGradleTest
import io.kotest.matchers.collections.shouldContainAll
import org.junit.jupiter.api.TestFactory
import java.io.File

class LatticePropertiesTest : LatticeGradleTest<GradleTestVersions> {

  override val kases: List<GradleTestVersions>
    get() = versionMatrix.versions(GradleTestVersions).takeLast(1)

  @TestFactory
  fun `vanniktech publishing properties become lattice properties`() = testFactory {

    rootProject {

      buildFile(
        """
        import com.rickbusarow.lattice.config.latticeProperties

        plugins {
          id("com.rickbusarow.lattice.root")
        }

        val ls = latticeProperties

        val printSettings by tasks.registering {
          doLast {
            println(ls)
          }
        }
        """.trimIndent()
      )

      file(
        "gradle/libs.versions.toml",
        File("../gradle/libs.versions.toml").readText()
      )

      gradlePropertiesFile(
        """
        lattice.versionName=0.1.0-SNAPSHOT

        POM_ARTIFACT_ID=pom artifact id
        POM_NAME=pom name
        POM_DESCRIPTION=pom description
        POM_INCEPTION_YEAR=pom inception year
        POM_URL=pom url

        POM_LICENSE_NAME=pom license name
        POM_LICENSE_URL=pom license url
        POM_LICENSE_DIST=pom license dist

        POM_SCM_URL=pom scm url
        POM_SCM_CONNECTION=pom scm connection
        POM_SCM_DEV_CONNECTION=pom scm dev connection

        POM_DEVELOPER_ID=pom developer id
        POM_DEVELOPER_NAME=pom developer name
        POM_DEVELOPER_URL=pom developer url
        """.trimIndent()
      )
    }

    shouldSucceed("printSettings", withPluginClasspath = true) {
      output.substringAfterLast("> :printSettings")
        .substringBefore("BUILD SUCCESSFUL")
        .lineSequence()
        .filter { it.isNotBlank() }
        .toList() shouldContainAll listOf(
        "lattice.publishing.pom.artifactId=pom artifact id",
        "lattice.publishing.pom.name=pom name",
        "lattice.publishing.pom.description=pom description",
        "lattice.publishing.pom.inceptionYear=pom inception year",
        "lattice.publishing.pom.url=pom url",
        "lattice.publishing.pom.license.name=pom license name",
        "lattice.publishing.pom.license.url=pom license url",
        "lattice.publishing.pom.license.dist=pom license dist",
        "lattice.publishing.pom.scm.url=pom scm url",
        "lattice.publishing.pom.scm.connection=pom scm connection",
        "lattice.publishing.pom.scm.devConnection=pom scm dev connection",
        "lattice.publishing.pom.developer.id=pom developer id",
        "lattice.publishing.pom.developer.name=pom developer name",
        "lattice.publishing.pom.developer.url=pom developer url"
      )
    }
  }
}
