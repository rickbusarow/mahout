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

import com.rickbusarow.kase.DefaultTestEnvironment
import com.rickbusarow.kase.HasTestEnvironmentFactory
import com.rickbusarow.kase.stdlib.div
import com.rickbusarow.kgx.newInstance
import com.rickbusarow.mahout.BaseMahoutExtension
import com.rickbusarow.mahout.mahoutExtensionAs
import io.kotest.matchers.shouldBe
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import javax.inject.Inject

class GradleTestsPluginTest :
  HasTestEnvironmentFactory<DefaultTestEnvironment.Factory> {

  override val testEnvironmentFactory = DefaultTestEnvironment.Factory()

  @Test
  fun `foo`() = test {

    val gradleTests = gradleTests()

    gradleTests.gradleTestM2Dir.get().asFile shouldBe workingDir / "build" / "gradle-test-m2"
  }

  private fun DefaultTestEnvironment.gradleTests(
    action: Action<GradleTestsSubExtension> = Action {}
  ): GradleTestsSubExtension {

    val root = ProjectBuilder.builder()
      .withProjectDir(workingDir)
      .withName("root")
      .build()

    val target = ProjectBuilder.builder()
      .withName("lib")
      .withProjectDir(workingDir / "lib")
      .withParent(root)
      .build()

    target.extensions.create("mahout", TestHasGradleTestsExtension::class.java)

    target.plugins.apply(GradleTestsPlugin::class.java)

    val mahout = target.mahoutExtensionAs<HasGradleTestsSubExtension>()

    with(mahout) { target.gradleTests(action) }

    return mahout.gradleTests
  }

  abstract class TestHasGradleTestsExtension @Inject constructor(
    target: Project,
    objects: ObjectFactory
  ) : BaseMahoutExtension(target, objects),
    HasGradleTestsSubExtension by objects.newInstance<DefaultHasGradleTestsSubExtension>()
}
