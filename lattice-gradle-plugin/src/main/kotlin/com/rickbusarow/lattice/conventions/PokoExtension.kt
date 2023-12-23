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

package com.rickbusarow.lattice.conventions

import com.rickbusarow.kgx.javaExtension
import com.rickbusarow.lattice.config.latticeProperties
import com.rickbusarow.lattice.deps.Modules
import com.rickbusarow.lattice.deps.PluginIds
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency

public interface HasCodeGenExtension {
  public val codeGen: CodeGenSubExtension
}

public interface CodeGenSubExtension {
  // public val generate: GenerateExtension
}

public interface HasPokoExtension {
  public val poko: PokoExtension
}

public interface PokoExtension {

  @Suppress("UndocumentedPublicFunction")
  public fun Project.poko() {
    // Poko adds its annotation artifact as 'implementation', which is unnecessary.
    // Replace it with a 'compileOnly' dependency.

    val annotationProvider = latticeProperties.versions.poko.map {
      dependencies.create("${Modules.`drewHamilton-poko-annotations`}:$it")
        as ExternalModuleDependency
    }

    val removeImplementation = lazy<Unit> {
      val dep = annotationProvider.get()
      val implementation = configurations.getByName("implementation")
      implementation.dependencies.remove(dep)
    }

    project.buildscript.dependencies.addProvider("classpath", annotationProvider)

    javaExtension.sourceSets.configureEach { sourceSet ->

      removeImplementation.value

      configurations
        .getByName(sourceSet.compileOnlyConfigurationName)
        .dependencies
        .addLater(annotationProvider)
    }

    pluginManager.apply(PluginIds.`drewHamilton-poko`)
  }
}
