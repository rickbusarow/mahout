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

import com.rickbusarow.kgx.javaExtension
import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.deps.Modules
import com.rickbusarow.mahout.deps.PluginIds
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency

/** */
public interface HasCodeGenExtension {

  /** */
  public val codeGen: CodeGenSubExtension
}

/** */
public interface CodeGenSubExtension {
  // public val generate: GenerateExtension
}

/** */
public interface HasPokoExtension {

  /** */
  public val poko: PokoExtension
}

/** */
public interface PokoExtension {

  @Suppress("UndocumentedPublicFunction")
  public fun Project.poko() {

    pluginManager.apply(PluginIds.`drewHamilton-poko`)

    // Poko adds its annotation artifact as 'implementation', which is unnecessary.
    // Replace it with a 'compileOnly' dependency.

    val pokoAnnotationsModule = Modules.`drewHamilton-poko-annotations`
    val annotationProvider = mahoutProperties.versions.poko.map {
      dependencies.create("$pokoAnnotationsModule:$it")
        as ExternalModuleDependency
    }

    javaExtension.sourceSets.configureEach { sourceSet ->

      // This needs to be in an `afterEvaluate` because it's added by Poko in `afterEvaluate`.
      //
      // Things that don't work:
      //
      // `implementation.withDependencies { implementation.remove(dep) }`
      //    - It's only invoked during resolution, so it will remove the dependency
      //      during an actual build, but it won't be removed for `./gradlew dependencies` or
      //      `./gradlew dependencyGuardBaseline`.
      //
      // `implementation.dependencies.whenObjectAdded {...}`
      //    - This forces all the `Provider<Dependency>`s to be evaluated eagerly.
      afterEvaluate {

        configurations.getByName(sourceSet.implementationConfigurationName)
          .dependencies
          .remove(annotationProvider.get())
      }

      configurations
        .getByName(sourceSet.compileOnlyConfigurationName)
        .dependencies
        .addLater(annotationProvider)
    }
  }
}
