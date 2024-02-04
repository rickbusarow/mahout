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

package com.rickbusarow.lattice.libs

import com.rickbusarow.kgx.libsCatalog
import com.rickbusarow.lattice.core.stdlib.applyEach
import com.rickbusarow.lattice.core.stdlib.capitalize
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/** */
public abstract class LibsGeneratorPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.tasks.register("generateLibs", LibsGeneratorTask::class.java) { task ->
      task.srcGenDir.set(target.layout.buildDirectory.dir("generated/source/kotlin"))
      task.packageName.set("boogers")
      task.catalogProvider.set(VersionCatalogSerializable(target.libsCatalog))
    }
  }
}

/** */
public abstract class LibsGeneratorTask @Inject constructor() : DefaultTask() {

/** */
  @get:OutputDirectory
  public abstract val srcGenDir: DirectoryProperty

/** */
  @get:Input
  public abstract val packageName: Property<String>

/** */
  @get:Input
  public abstract val catalogProvider: Property<VersionCatalogSerializable>

  @TaskAction
  public fun generateLibs() {

    val catalog = catalogProvider.get()

    val versionValues = catalog.versions // .asNodes()
    val plugins = catalog.plugins // .asNodes()
    val libraries = catalog.libraries // .asNodes()
    val bundles = catalog.bundles // .asNodes()

    val ts = TypeSpec.classBuilder(catalog.name.capitalize())
      .addModifiers(KModifier.PUBLIC)
      .addSuperinterface(java.io.Serializable::class)
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .applyEach(versionValues) { (alias, version) ->
            addParameter(
              ParameterSpec
                .builder(alias, String::class)
                .defaultValue("%S", version)
                .build()
            )
          }
          .build()
      )
      .applyEach(versionValues) { (alias, _) ->
        addProperty(
          PropertySpec.builder(alias, String::class)
            .initializer(alias)
            .build()
        )
      }
      .applyEach(plugins) { (alias, pluginId) ->
        addProperty(
          PropertySpec.builder(alias, String::class)
            .initializer("%S", pluginId)
            .build()
        )
      }
      .applyEach(libraries) { (alias, library) ->
        addProperty(
          PropertySpec.builder(alias, String::class)
            .initializer("%S", library)
            .build()
        )
      }
      .applyEach(bundles) { (alias, bundle) ->
        addProperty(
          PropertySpec.builder(alias, String::class)
            .initializer("%S", bundle)
            .build()
        )
      }
      .build()

    val fs = FileSpec.builder(packageName.get(), catalog.name.capitalize())
      .addType(ts)
      .build()

    fs.writeTo(srcGenDir.get().asFile)
      // TODO <Rick> delete me
      .also(::println)
  }
}
