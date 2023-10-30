/*
 * Copyright (C) 2023 Rick Busarow
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

package com.rickbusarow.antipasto.conventions

import com.rickbusarow.antipasto.core.commonPropertyPrefix
import com.rickbusarow.antipasto.core.prefixedPropertyOrNull
import com.rickbusarow.kgx.isRealRootProject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.internal.classpath.Instrumented.systemProperty

@Suppress("UndocumentedPublicClass")
public abstract class TestConventionPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    val includeTags: ListProperty<String> = target.objects
      .listProperty(String::class.java)

    val tagsString = target.prefixedPropertyOrNull<String>("includeTags")

    if (tagsString != null) {
      includeTags.addAll(tagsString.split(','))
    }

    target.tasks.withType(Test::class.java).configureEach { task ->
      task.useJUnitPlatform()

      val junitPlatformOptions = task.testFrameworkProperty
        .map { frameWork ->
          @Suppress("ForbiddenImport")
          (frameWork as org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestFramework).options
        }

      task.doFirst {

        val tags = includeTags.orNull
        if (!tags.isNullOrEmpty()) {
          junitPlatformOptions.get().includeTags(*tags.toTypedArray())
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

      // Allow JUnit4 tests to run in parallel
      task.maxParallelForks = Runtime.getRuntime().availableProcessors()

      if (target.isRealRootProject()) {
        val thisTaskName = task.name
        target.subprojects { sub ->
          task.dependsOn(sub.tasks.matching { it.name == thisTaskName })
        }
      }
    }
  }
}
