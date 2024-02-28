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

package conventions

import com.rickbusarow.kgx.gradleProperty
import com.rickbusarow.kgx.kotlinJvmExtension
import com.rickbusarow.kgx.withKotlinJvmPlugin
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.PluginDeclaration
import org.gradle.plugins.ide.idea.model.IdeaModel
import javax.inject.Inject
import kotlin.reflect.KClass
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

/** */
abstract class DogFoodPlugin : Plugin<Project> {

  override fun apply(target: Project) {
    target.extensions.create("dogFood", DogFoodExtension::class.java)

    target.pluginManager.apply("idea")

    moveBuildDirs(target)

    optIn(target)
  }

  private fun optIn(target: Project) {
    val excluded = setOf(
      "mahout-api",
      "mahout-settings-annotations",
      "mahout-settings-generator"
    )
    if (target.name !in excluded) {
      target.plugins.withKotlinJvmPlugin {
        target.kotlinJvmExtension
          .compilerOptions
          .optIn
          .add("com.rickbusarow.mahout.core.InternalMahoutApi")
      }
    }
  }

  private fun moveBuildDirs(target: Project) {
    if (target.rootProject.name == "mahout") {
      target.layout.buildDirectory.set(target.file("build/main"))
      target.extensions.configure(IdeaModel::class.java) {
        it.module { ideaModule ->
          ideaModule.generatedSourceDirs.add(target.file("build"))
          ideaModule.excludeDirs.add(target.file("build"))
        }
      }
    } else {
      target.layout.buildDirectory.set(target.file("build/dogFood"))
    }
  }
}

/** */
@DslMarker
annotation class DogFoodDsl

/** */
@DogFoodDsl
abstract class DogFoodExtension @Inject constructor(private val target: Project) {

  fun mainMahoutPlugin(id: String) {
    if (target.rootProject.name == "mahout") {
      target.pluginManager.apply(id)
    }
  }

  /** */
  fun publishMaven(
    artifactId: String,
    name: String,
    description: String
  ) {
    if (target.rootProject.name == "mahout") {

      target.extensions.extraProperties.let { props ->
        for ((key, value) in arrayOf(
          "mahout.publishing.pom.artifactId" to artifactId,
          "mahout.publishing.pom.name" to name,
          "mahout.publishing.pom.description" to description
        )) {
          props.set(key, value)
        }
      }

      val mahoutBase = target.extensions.getByName("mahout")

      val publishingProperty = mahoutBase::class.memberProperties
        .single { it.name == "publishing" }

      val publishMavenFun = (publishingProperty.returnType.classifier!! as KClass<*>)
        .memberFunctions
        .single { it.name == "publishMaven" }

      val publishingSub = publishingProperty.call(mahoutBase)

      fun p(name: String) = publishMavenFun.parameters.single { it.name == name }

      val params = mapOf(
        publishMavenFun.instanceParameter!! to publishingSub,
        p("artifactId") to artifactId,
        p("pomDescription") to description,
        p("groupId") to target.gradleProperty("mahout.group"),
        p("versionName") to target.gradleProperty("mahout.versionName"),
        p("sourceSetName") to "main",
        p("publicationName") to null,
        p("configureAction") to null
      )

      publishMavenFun.callBy(params)
    }
  }

  fun plugin(
    name: String,
    implementationClass: String,
    description: String,
    additionalTags: List<String>
  ): NamedDomainObjectProvider<PluginDeclaration> {

    return target.extensions.getByType(GradlePluginDevelopmentExtension::class.java)
      .plugins
      .register(name) { declaration ->
        declaration.id = "com.rickbusarow.mahout.$name"
        declaration.implementationClass = implementationClass
        declaration.description = description
        @Suppress("UnstableApiUsage")
        declaration.tags.addAll("convention-plugin", "kotlin", *additionalTags.toTypedArray())
      }
  }
}
