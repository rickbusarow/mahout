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

package com.rickbusarow.mahout.config

import com.rickbusarow.mahout.MahoutGradleTest
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainAll
import org.junit.jupiter.api.TestFactory
import java.io.File

class MahoutPropertiesTest : MahoutGradleTest {

  @TestFactory
  fun `vanniktech publishing properties become mahout properties`() = testFactory { _ ->

    rootProject {

      buildFile(
        // language=kotlin
        """
        plugins {
          id("com.rickbusarow.mahout.root")
        }

        val props = mahoutProperties

        val printSettings by tasks.registering {
          doLast {
            println("group=${'$'}{project.group}")
            println("version=${'$'}version")
            println(props)
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
        mahout.versionName=0.1.0-SNAPSHOT
        mahout.group=com.rickbusarow.test

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

      output.asClue { _ ->

        output.substringAfterLast("> :printSettings")
          .substringBefore("BUILD SUCCESSFUL")
          .lineSequence()
          .filter { it.isNotBlank() }
          .toList() shouldContainAll listOf(
          "group=com.rickbusarow.test",
          "version=0.1.0-SNAPSHOT",
          "mahout.publishing.pom.artifactId=pom artifact id",
          "mahout.publishing.pom.name=pom name",
          "mahout.publishing.pom.description=pom description",
          "mahout.publishing.pom.inceptionYear=pom inception year",
          "mahout.publishing.pom.url=pom url",
          "mahout.publishing.pom.license.name=pom license name",
          "mahout.publishing.pom.license.url=pom license url",
          "mahout.publishing.pom.license.dist=pom license dist",
          "mahout.publishing.pom.scm.url=pom scm url",
          "mahout.publishing.pom.scm.connection=pom scm connection",
          "mahout.publishing.pom.scm.devConnection=pom scm dev connection",
          "mahout.publishing.pom.developer.id=pom developer id",
          "mahout.publishing.pom.developer.name=pom developer name",
          "mahout.publishing.pom.developer.url=pom developer url"
        )
      }
    }
  }
}
