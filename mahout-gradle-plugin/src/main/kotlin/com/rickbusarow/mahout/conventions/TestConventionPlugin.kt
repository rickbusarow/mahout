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

import com.rickbusarow.kgx.buildDir
import com.rickbusarow.kgx.dependsOn
import com.rickbusarow.kgx.isRealRootProject
import com.rickbusarow.kgx.withJavaPlugin
import com.rickbusarow.mahout.api.MahoutCheckTask
import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.core.check
import com.rickbusarow.mahout.core.commonPropertyPrefix
import com.rickbusarow.mahout.core.javaToolchainService
import com.rickbusarow.mahout.core.prefixedPropertyOrNull
import com.rickbusarow.mahout.deps.Libs
import com.rickbusarow.mahout.deps.Versions
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.jvm.JvmTestSuite
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.junitplatform.JUnitPlatformOptions
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.internal.classpath.Instrumented.systemProperty
import org.gradle.testing.base.TestingExtension

/** */
public abstract class TestConventionPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    val includeTags: ListProperty<String> = target.objects
      .listProperty(String::class.java)

    val tagsString = target.prefixedPropertyOrNull<String>("includeTags")

    if (tagsString != null) {
      includeTags.addAll(tagsString.split(','))
    }

    target.tasks.withType(Test::class.java).configureEach { task ->
      task.systemProperty("kase.baseWorkingDir", target.buildDir().resolve("kase"))

      task.doFirst {

        val tags = includeTags.orNull
        if (!tags.isNullOrEmpty()) {
          (task.options as JUnitPlatformOptions).includeTags(*tags.toTypedArray())
        }
      }

      task.testLogging {
        it.events = setOf(FAILED)
        it.exceptionFormat = TestExceptionFormat.FULL
        it.showExceptions = true
        it.showCauses = true
        it.showStackTraces = true
      }

      val commonPrefix = target.commonPropertyPrefix

      target.properties
        .filter { (key, value) ->
          key.startsWith("$commonPrefix.") && value != null
        }
        .forEach { (key, value) ->
          systemProperty(key, value.toString())
        }

      task.maxHeapSize = "4g"

      task.systemProperties.putAll(
        mapOf(

          // auto-discover and apply any Junit5 extensions in the classpath
          // "junit.jupiter.extensions.autodetection.enabled" to true,

          // remove parentheses from test display names
          "junit.jupiter.displayname.generator.default" to
            "org.junit.jupiter.api.DisplayNameGenerator\$Simple",

          // https://junit.org/junit5/docs/snapshot/user-guide/#writing-tests-parallel-execution-config-properties
          // Allow unit tests to run in parallel
          "junit.jupiter.execution.parallel.enabled" to true,
          "junit.jupiter.execution.parallel.mode.default" to "concurrent",
          "junit.jupiter.execution.parallel.mode.classes.default" to "concurrent",

          "junit.jupiter.execution.parallel.config.strategy" to "dynamic",
          "junit.jupiter.execution.parallel.config.dynamic.factor" to 1.0
        )
      )

      // Allow JUnit tests to run in parallel
      task.maxParallelForks = Runtime.getRuntime().availableProcessors()

      if (target.isRealRootProject()) {
        val thisTaskName = task.name
        target.subprojects { sub ->
          task.dependsOn(sub.tasks.matching { it.name == thisTaskName })
        }
      }
    }

    target.plugins.withJavaPlugin {

      @Suppress("UnstableApiUsage")
      target.extensions.getByType(TestingExtension::class.java)
        .suites
        .withType(JvmTestSuite::class.java)
        .configureEach { suite ->
          suite.useJUnitJupiter(Versions.jUnit5)
          suite.dependencies {
            it.runtimeOnly.add(Libs.`junit-vintage-engine`)

            // https://junit.org/junit5/docs/current/user-guide/#running-tests-build-gradle-bom
            // https://github.com/junit-team/junit5/issues/4374#issuecomment-2704880447
            it.implementation.add(Libs.`junit-jupiter`)
            it.runtimeOnly.add(Libs.`junit-platform-launcher`)
          }
        }

      val javaSettings = target.mahoutProperties.java

      val testAll = target.tasks.register("testAll", Test::class.java) { task ->
        task.description = "Run all tests"
        task.group = "Verification"
      }

      for (jdk in javaSettings.testJvmTargets.getOrElse(emptyList())) {
        val testJdk = target.tasks.register(
          "testJdk${jdk.major}",
          MahoutTestJdkTask::class.java
        ) { task ->

          task.javaLauncher.set(
            target.javaToolchainService
              .launcherFor { it.languageVersion.set(jdk.javaLanguageVersion) }
          )

          task.description = "test using JDK ${jdk.major}"
          task.group = "Verification"

          val testTask = target.tasks.named("test", Test::class.java).get()

          task.classpath = testTask.classpath
          task.testClassesDirs = testTask.testClassesDirs
        }
        target.tasks.check.dependsOn(testJdk)
        testAll.dependsOn(testJdk)
      }
    }
  }
}

/** A [Test] task that has an overridden `javaLauncher` property. */
public abstract class MahoutTestJdkTask : Test(), MahoutCheckTask
