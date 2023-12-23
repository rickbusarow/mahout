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

package com.rickbusarow.lattice.conventions

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import com.rickbusarow.kgx.applyOnce
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

public interface BuildLogicShadowExtensionHook {

  // val shadow: BuildLogicShadowExtension

  @Suppress("UndocumentedPublicFunction")
  public fun shadow() {
    // shadow.enabled.set(false)
  }

  @Suppress("UndocumentedPublicFunction")
  public fun shadow(enabled: Boolean) {
    // shadow.enabled.set(false)
  }

  //   public    fun shadow4(
  //   shadowConfiguration: Configuration?
  // )

  //    public   fun shadow5(
  //   shadowConfiguration: Configuration,
  //   relocatePackages: List<String>
  // )
}

public abstract class BuildLogicShadowExtension @Inject constructor(
  objects: ObjectFactory
) {
  @Suppress("UndocumentedPublicProperty")
  public val enabled: Property<Boolean> = objects.property(Boolean::class.java)

  @Suppress("UndocumentedPublicProperty")
  public abstract val relocatePackages: ListProperty<String>

  @Suppress("UndocumentedPublicFunction")
  public fun Project.shadow(
    shadowConfiguration: Configuration?,
    relocatePackages: List<String> = emptyList()
  ) {

    plugins.applyOnce("com.github.johnrengelman.shadow")

    val classifier = if (plugins.hasPlugin("java-gradle-plugin")) "" else "all"

    if (shadowConfiguration != null) {
      configurations.named("compileOnly") { it.extendsFrom(shadowConfiguration) }
    }

    val shadowJar = tasks.named("shadowJar", ShadowJar::class.java) { task ->

      task.configurations = listOf(shadowConfiguration)

      for (it in relocatePackages) {
        task.relocate(it, "$group.$it")
      }

      task.archiveClassifier.convention(classifier)
      task.archiveClassifier.set(classifier)

      task.transformers.add(ServiceFileTransformer())

      task.minimize()

      // Excluding these helps shrink our binary dramatically
      task.exclude("**/*.kotlin_metadata")
      task.exclude("**/*.kotlin_module")
      task.exclude("META-INF/maven/**")
    }

    // By adding the task's output to archives, it's automatically picked up by Gradle's maven-publish
    // plugin and added as an artifact to the publication.
    artifacts {
      it.add("runtimeOnly", shadowJar)
      it.add("archives", shadowJar)
    }
  }
}
