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

package com.rickbusarow.mahout

import com.rickbusarow.kase.AbstractKase3
import com.rickbusarow.kase.Kase3
import com.rickbusarow.kase.KaseMatrix
import com.rickbusarow.kase.files.HasWorkingDir
import com.rickbusarow.kase.files.JavaFileFileInjection
import com.rickbusarow.kase.files.LanguageInjection
import com.rickbusarow.kase.files.TestLocation
import com.rickbusarow.kase.gradle.DefaultGradleTestEnvironment
import com.rickbusarow.kase.gradle.DslLanguage
import com.rickbusarow.kase.gradle.GradleDependencyVersion
import com.rickbusarow.kase.gradle.GradleKotlinTestVersions
import com.rickbusarow.kase.gradle.GradleProjectBuilder
import com.rickbusarow.kase.gradle.GradleRootProjectBuilder
import com.rickbusarow.kase.gradle.GradleTestEnvironmentFactory
import com.rickbusarow.kase.gradle.HasDslLanguage
import com.rickbusarow.kase.gradle.KaseGradleTest
import com.rickbusarow.kase.gradle.KotlinDependencyVersion
import com.rickbusarow.kase.gradle.dsl.BuildFileSpec
import com.rickbusarow.kase.gradle.rootProject
import com.rickbusarow.kase.gradle.versions
import java.io.File
import kotlin.LazyThreadSafetyMode.NONE

interface MahoutGradleTest :
  KaseGradleTest<MahoutGradleTestParams, MahoutGradleTestEnvironment, MahoutGradleTestEnvironment.Factory> {

  override val testEnvironmentFactory get() = MahoutGradleTestEnvironment.Factory()

  override val kaseMatrix: KaseMatrix
    get() = MahoutVersionMatrix()

  override val params: List<MahoutGradleTestParams>
    get() = params(DslLanguage.KotlinDsl(useInfix = true, useLabels = false))

  fun params(dslLanguage: DslLanguage): List<DefaultMahoutGradleTestParams> {
    return kaseMatrix.versions(GradleKotlinTestVersions)
      .map { (gradle, kotlin) ->
        DefaultMahoutGradleTestParams(
          dslLanguage = dslLanguage,
          gradle = gradle,
          kotlin = kotlin
        )
      }
  }
}

interface MahoutGradleTestParams :
  Kase3<GradleDependencyVersion, KotlinDependencyVersion, DslLanguage>,
  HasDslLanguage,
  GradleKotlinTestVersions

class DefaultMahoutGradleTestParams(
  override val dslLanguage: DslLanguage,
  override val gradle: GradleDependencyVersion,
  override val kotlin: KotlinDependencyVersion
) : AbstractKase3<GradleDependencyVersion, KotlinDependencyVersion, DslLanguage>(
  a1 = gradle,
  a2 = kotlin,
  a3 = dslLanguage
),
  MahoutGradleTestParams {

  override val gradleVersion: String get() = gradle.value
  override val kotlinVersion: String get() = kotlin.value

  override val displayName: String by lazy(NONE) {
    "dsl: ${dslLanguage::class.simpleName} | gradle: $gradle | kotlin: $kotlin"
  }
}

class MahoutGradleTestEnvironment(
  gradleVersion: GradleDependencyVersion,
  override val dslLanguage: DslLanguage,
  hasWorkingDir: HasWorkingDir,
  override val rootProject: GradleRootProjectBuilder
) : DefaultGradleTestEnvironment(
  gradleVersion = gradleVersion,
  dslLanguage = dslLanguage,
  hasWorkingDir = hasWorkingDir,
  rootProject = rootProject
),
  LanguageInjection<File> by LanguageInjection(JavaFileFileInjection()) {

  val mahoutVersion: String
    get() = GradleTestBuildConfig.mahoutVersion

  val GradleProjectBuilder.buildFileAsFile: File
    get() = path.resolve(dslLanguage.buildFileName)
  val GradleProjectBuilder.settingsFileAsFile: File
    get() = path.resolve(dslLanguage.settingsFileName)

  class Factory : GradleTestEnvironmentFactory<MahoutGradleTestParams, MahoutGradleTestEnvironment> {

    override val localM2Path: File
      get() = GradleTestBuildConfig.buildM2Dir

    override fun buildFileDefault(versions: MahoutGradleTestParams): BuildFileSpec =
      BuildFileSpec {
        plugins {
          kotlin("jvm", versions.kotlinVersion)
          id("com.rickbusarow.mahout.root", version = GradleTestBuildConfig.mahoutVersion)
        }
      }

    override fun create(
      params: MahoutGradleTestParams,
      names: List<String>,
      location: TestLocation
    ): MahoutGradleTestEnvironment {
      val hasWorkingDir = HasWorkingDir(names, location)

      return MahoutGradleTestEnvironment(
        gradleVersion = params.gradle,
        dslLanguage = params.dslLanguage,
        hasWorkingDir = hasWorkingDir,
        rootProject = rootProject(
          path = hasWorkingDir.workingDir,
          dslLanguage = params.dslLanguage
        ) {
          buildFile(buildFileDefault(params))
          settingsFile(settingsFileDefault(params))
        }
      )
    }
  }
}
