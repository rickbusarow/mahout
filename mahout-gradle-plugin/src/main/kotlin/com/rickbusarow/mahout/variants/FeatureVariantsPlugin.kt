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

package com.rickbusarow.mahout.variants

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPluginExtension
import com.rickbusarow.kgx.dependsOn
import com.rickbusarow.kgx.gradleLazy
import com.rickbusarow.kgx.javaExtension
import com.rickbusarow.kgx.mustRunAfter
import com.rickbusarow.kgx.named
import com.rickbusarow.kgx.names.ConfigurationName
import com.rickbusarow.kgx.names.ConfigurationName.Companion.apiConfig
import com.rickbusarow.kgx.names.ConfigurationName.Companion.compileOnlyConfig
import com.rickbusarow.kgx.names.ConfigurationName.Companion.implementationConfig
import com.rickbusarow.kgx.names.ConfigurationName.Companion.kspConfig
import com.rickbusarow.kgx.names.ConfigurationName.Companion.runtimeOnlyConfig
import com.rickbusarow.kgx.names.SourceSetName
import com.rickbusarow.kgx.names.SourceSetName.Companion.addPrefix
import com.rickbusarow.kgx.names.SourceSetName.Companion.addSuffix
import com.rickbusarow.kgx.names.SourceSetName.Companion.asSourceSetName
import com.rickbusarow.kgx.names.TaskName
import com.rickbusarow.kgx.names.TaskName.Companion.asTaskName
import com.rickbusarow.kgx.names.TaskName.Companion.compileKotlin
import com.rickbusarow.kgx.register
import com.rickbusarow.mahout.api.SubExtension
import com.rickbusarow.mahout.api.SubExtensionInternal
import com.rickbusarow.mahout.conventions.AbstractHasSubExtension
import com.rickbusarow.mahout.conventions.AbstractSubExtension
import com.vanniktech.maven.publish.tasks.JavadocJar
import dev.drewhamilton.poko.Poko
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.internal.catalog.ExternalModuleDependencyFactory.DependencyNotationSupplier
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.signing.Sign
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaCompilation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.io.Serializable
import javax.inject.Inject
import kotlin.reflect.KProperty

public abstract class FeatureVariantsPlugin : Plugin<Project> {

  override fun apply(target: Project) {

    val jvmModuleExtension = target.extensions.getByName("jvmModule") as ExtensionAware

    jvmModuleExtension.extensions
      .create("featureVariants", FeatureVariantsSubExtension::class.java)
  }
}

/** */
public interface HasFeatureVariantsSubExtension : Serializable {

  /** */
  public val variants: FeatureVariantsSubExtension

  /** */
  public fun variants(action: Action<in FeatureVariantsSubExtension>) {
    action.execute(variants)
  }
}

internal abstract class DefaultHasFeatureVariantsSubExtension @Inject constructor(
  override val objects: ObjectFactory
) : AbstractHasSubExtension(),
  HasFeatureVariantsSubExtension,
  SubExtensionInternal {

  override val variants: FeatureVariantsSubExtension by
    subExtension(DefaultFeatureVariantsSubExtension::class)
}

public interface FeatureVariantsSubExtension : SubExtension<FeatureVariantsSubExtension> {

  public val variants: NamedDomainObjectContainer<FeatureVariant>
  public fun variant(name: String, dependencies: Action<FeatureVariantDependencyScope>)
}

public abstract class DefaultFeatureVariantsSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : AbstractSubExtension(target, objects),
  FeatureVariantsSubExtension,
  SubExtensionInternal {

  public override val variants: NamedDomainObjectContainer<FeatureVariant> =
    objects.domainObjectContainer(FeatureVariant::class.java)

  public override fun variant(name: String, dependencies: Action<FeatureVariantDependencyScope>) {

    variants.create(name) { variant ->
      dependencies.execute(variant.dependencyScope)
      val capabilities = variant.dependencyScope.capabilities

      target.javaExtension.registerFeature(name) { spec ->
        spec.usingSourceSet(variant.prodSourceSet)

        for (capability in capabilities) {
          val c = capability.get()
          spec.capability(c.group, c.name, c.version)
        }
      }

      target.extensions.getByType(DependencyGuardPluginExtension::class.java)
        .configuration(variant.prodSourceSet.runtimeClasspathConfigurationName)

      target.tasks.named("check").dependsOn(variant.testTaskName)
      target.tasks.named("testAll").dependsOn(variant.testTaskProvider)

      target.extensions.configure(IdeaModel::class.java) { idea ->
        idea.module { module ->
          module.testSources.from(variant.testSourceSet.map { it.allSource.srcDirs })
        }
      }
      createPublication(
        target = target,
        featureVariant = variant,
        artifactId = variant.artifactId,
        publicationName = variant.prodSourceSetName.value,
        dokkaJavadocJar = target.tasks.named("dokkaJavadocJar", JavadocJar::class.java)
      )
    }
  }
}

public abstract class FeatureVariant @Inject constructor(
  public val name: String,
  target: Project
) : Serializable {

  public val kotlin: KotlinJvmProjectExtension = target.kotlinExtension as KotlinJvmProjectExtension
  public val kotlinMainCompilation:
    KotlinWithJavaCompilation<KotlinJvmOptions, KotlinJvmCompilerOptions>
    by kotlin.target.compilations.named(SourceSet.MAIN_SOURCE_SET_NAME)
  public val kotlinMainTestCompilation:
    KotlinWithJavaCompilation<KotlinJvmOptions, KotlinJvmCompilerOptions>
    by kotlin.target.compilations.named(SourceSet.TEST_SOURCE_SET_NAME)

  public val java: JavaPluginExtension by gradleLazy { target.javaExtension }

  public val prodSourceSetName: SourceSetName = name.asSourceSetName()

  public val testSourceSetName: SourceSetName = prodSourceSetName.addSuffix(SourceSetName.test)
  public val testTaskName: TaskName = testSourceSetName.addPrefix(TaskName.test)
  public val jarTaskName: TaskName = prodSourceSetName.addSuffix(TaskName.jar)
  public val sourcesJarTaskName: TaskName = prodSourceSetName.addSuffix("SourcesJar".asTaskName())

  public val artifactId: String = "ktrules-${prodSourceSetName.value.substringAfter("compat")}"

  public val kotlinDir: File = target.file("src/${prodSourceSetName.value}/kotlin")
  public val resourceDir: File = target.file("src/${prodSourceSetName.value}/resources")
  public val testKotlinDir: File = target.file("src/${testSourceSetName.value}/kotlin")
  public val testResourceDir: File = target.file("src/${testSourceSetName.value}/resources")

  public val main: SourceSet by java.sourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME)
  public val kotlinMain: KotlinSourceSet by target.kotlinExtension.sourceSets.named(
    SourceSet.MAIN_SOURCE_SET_NAME
  )

  public val test: SourceSet by java.sourceSets.named(SourceSet.TEST_SOURCE_SET_NAME)
  public val kotlinTest: KotlinSourceSet by target.kotlinExtension.sourceSets.named(
    SourceSet.TEST_SOURCE_SET_NAME
  )

  public val prodSourceSet: SourceSet by java.sourceSets.register(prodSourceSetName) { ss ->
    ss.java.srcDir(kotlinDir)
    ss.resources.srcDir(resourceDir)
    ss.compileClasspath += main.output
    ss.runtimeClasspath += main.output
  }

  public val kotlinProdSourceSet: NamedDomainObjectProvider<KotlinSourceSet> =
    target.kotlinExtension.sourceSets
      .named(prodSourceSetName) { kss ->
        kss.dependsOn(kotlinMain)
        kss.kotlin.srcDir(kotlinDir)
        kss.resources.srcDir(resourceDir)
      }

  public val testSourceSet: NamedDomainObjectProvider<SourceSet> =
    java.sourceSets.register(testSourceSetName) { ss ->

      ss.java.srcDir(testKotlinDir)
      ss.resources.srcDir(testResourceDir)
      ss.compileClasspath += (ss.output + test.output)
      ss.runtimeClasspath += (ss.output + test.output)
    }

  public val kotlinTestSourceSet: KotlinSourceSet by target.kotlinExtension.sourceSets
    .named(testSourceSetName) { kss ->
      kss.dependsOn(kotlinTest)
      // kss.dependsOn(kotlinProdSourceSet.get())
      kss.kotlin.srcDir(testKotlinDir)
      kss.resources.srcDir(testResourceDir)
    }

  public val jarTaskProvider: TaskProvider<Jar> = target.tasks.register(jarTaskName, Jar::class) {
    it.from(prodSourceSet.output)

    // archiveFileName.set("$prodSourceSetName.jar")
    it.archiveClassifier.set(prodSourceSetName.value)
  }
  public val sourcesJarTaskProvider: TaskProvider<Jar> =
    target.tasks.register(sourcesJarTaskName, Jar::class) {
      it.archiveClassifier.set("${prodSourceSetName.value}-sources")
      it.from(prodSourceSet.allSource)
    }

  public val testTaskProvider: TaskProvider<Test> =
    target.tasks.register(testTaskName, Test::class) {
      it.description = "Runs tests for the ${prodSourceSetName.value} source set."
      it.group = "Verification"

      it.testClassesDirs = testSourceSet.get().output.classesDirs
      it.classpath = testSourceSet.get().runtimeClasspath
    }

  public val compileKotlinTaskName: TaskName = prodSourceSetName.compileKotlin()
  public val compileTestKotlinTaskName: TaskName = testSourceSetName.compileKotlin()

  public val compileKotlinTask: KotlinCompile by target.tasks.named(compileKotlinTaskName)
  public val compileTestKotlinTask: KotlinCompile by target.tasks.named(compileTestKotlinTaskName)

  public val prodCompilation: KotlinWithJavaCompilation<KotlinJvmOptions, KotlinJvmCompilerOptions>
    by kotlin.target.compilations.named(prodSourceSetName) {
      // it.associateWith(kotlinMainCompilation)
    }

  public val testSourceSetCompilation:
    KotlinWithJavaCompilation<KotlinJvmOptions, KotlinJvmCompilerOptions>
    by kotlin.target.compilations.named(testSourceSetName) {

      it.associateWith(kotlinMainCompilation)
      // it.associateWith(prodCompilation)
    }

  public val configurations: ConfigurationContainer = target.configurations

  public val kspConfigName: ConfigurationName = prodSourceSetName.kspConfig()
  public val kspConfig: Configuration by configurations.named(kspConfigName)

  public val apiConfig: Configuration by configurations.named(prodSourceSetName.apiConfig())
  public val compileOnlyConfig: Configuration
    by configurations.named(prodSourceSetName.compileOnlyConfig())
  public val implementationConfig: Configuration
    by configurations.named(prodSourceSetName.implementationConfig())
  public val runtimeOnlyConfig: Configuration
    by configurations.named(prodSourceSetName.runtimeOnlyConfig())
  public val testImplementationConfig: Configuration
    by configurations.named(testSourceSetName.implementationConfig())

  public val dependencyScope: FeatureVariantDependencyScope by gradleLazy {
    FeatureVariantDependencyScope(
      apiConfig = apiConfig,
      implementationConfig = implementationConfig,
      testImplementationConfig = testImplementationConfig,
      compileOnlyConfig = compileOnlyConfig
    )
  }
}

public fun createPublication(
  target: Project,
  featureVariant: FeatureVariant,
  artifactId: String,
  publicationName: String,
  dokkaJavadocJar: Provider<JavadocJar>
) {
  target.extensions.getByType(PublishingExtension::class.java)
    .publications
    .register(publicationName, MavenPublication::class.java) { publication ->

      publication.artifact(featureVariant.jarTaskProvider)
      publication.artifact(dokkaJavadocJar)
      publication.artifact(featureVariant.sourcesJarTaskProvider)

      publication.artifactId = artifactId

      publication.pom.name.set(artifactId)

      publication.suppressPomMetadataWarningsFor(featureVariant.prodSourceSetName.value)

      publication.pom.withXml {
        val dependenciesList = it.asNode().get("dependencies") as NodeList
        val node = dependenciesList.firstOrNull() as? Node
          ?: it.asNode().appendNode("dependencies")

        buildSet {
          addAll(featureVariant.apiConfig.allDependencies)
          addAll(featureVariant.implementationConfig.allDependencies)
          addAll(featureVariant.runtimeOnlyConfig.allDependencies)
        }
          .forEach { dep ->

            node.appendNode("dependency").also { depNode ->
              depNode.appendNode("groupId", dep.group)
              depNode.appendNode("artifactId", dep.name)
              depNode.appendNode("version", dep.version)
              depNode.appendNode("scope", "runtime")
            }
          }
      }
    }

  target.tasks.withType(Sign::class.java)
    .mustRunAfter(target.tasks.withType(Jar::class.java))

  target.tasks.withType(AbstractPublishToMaven::class.java)
    .matching { it.name.contains(featureVariant.prodSourceSetName.value) }
    .mustRunAfter(target.tasks.withType(Sign::class.java))
}

public inline operator fun <T : Any, reified U : T> NamedDomainObjectProvider<out T>.getValue(
  thisRef: Any?,
  property: KProperty<*>
): U = get().let { it as U }

public operator fun <T : Any> T.provideDelegate(receiver: Any?, property: KProperty<*>): T = this

/**
 * Just a Maven coordinate broken down into three segments.
 *
 * @property group ex: `com.pinterest.ktlint`
 * @property name ex: `ktlint-core`
 * @property version ex: `0.47.1`
 */
@Poko
public class VariantCapability(
  public val group: String,
  public val name: String,
  public val version: String
)

public class FeatureVariantDependencyScope(
  private val apiConfig: Configuration,
  private val implementationConfig: Configuration,
  private val testImplementationConfig: Configuration,
  private val compileOnlyConfig: Configuration
) {

  public val capabilities: MutableList<Provider<VariantCapability>> =
    mutableListOf<Provider<VariantCapability>>()

  private fun capability(notation: Provider<MinimalExternalModuleDependency>) {
    capabilities.add(
      notation.map {
        VariantCapability(
          group = it.group!!,
          name = it.name,
          version = it.version!!
        )
      }
    )
  }

  public fun api(dependencyNotation: Provider<MinimalExternalModuleDependency>) {
    apiConfig.dependencies.addLater(dependencyNotation)
    capability(dependencyNotation)
  }

  public fun compileOnly(dependencyNotation: Provider<MinimalExternalModuleDependency>) {
    compileOnlyConfig.dependencies.addLater(dependencyNotation)
    capability(dependencyNotation)
  }

  public fun implementation(dependencyNotation: Provider<MinimalExternalModuleDependency>) {
    implementationConfig.dependencies.addLater(dependencyNotation)
    capability(dependencyNotation)
  }

  public fun testImplementation(dependencyNotation: DependencyNotationSupplier) {
    testImplementation(dependencyNotation.asProvider())
  }

  public fun testImplementation(dependencyNotation: Provider<MinimalExternalModuleDependency>) {
    testImplementationConfig.dependencies.addLater(dependencyNotation)
  }
}
