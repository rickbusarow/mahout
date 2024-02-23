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
import com.rickbusarow.kgx.javaExtension
import com.rickbusarow.kgx.mustRunAfter
import com.rickbusarow.mahout.api.SubExtension
import com.rickbusarow.mahout.api.SubExtensionInternal
import com.rickbusarow.mahout.conventions.AbstractHasSubExtension
import com.rickbusarow.mahout.conventions.AbstractSubExtension
import com.rickbusarow.mahout.dokka.DokkatooConventionPlugin.Companion.dokkaJavadocJar
import groovy.util.Node
import groovy.util.NodeList
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.signing.Sign
import java.io.Serializable
import javax.inject.Inject

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

/** */
public interface FeatureVariantsSubExtension : SubExtension<FeatureVariantsSubExtension> {

  /** */
  public val variants: NamedDomainObjectContainer<FeatureVariant>

  /** */
  public fun variant(name: String, config: Action<FeatureVariant>)
}

/** */
public abstract class DefaultFeatureVariantsSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : AbstractSubExtension(target, objects),
  FeatureVariantsSubExtension,
  SubExtensionInternal {

  public override val variants: NamedDomainObjectContainer<FeatureVariant> =
    objects.domainObjectContainer(FeatureVariant::class.java)

  public override fun variant(name: String, config: Action<FeatureVariant>) {

    variants.create(name) { variant ->

      config.execute(variant)

      // val capabilities = variant.dependencyScope.capabilities
      @Suppress("UnstableApiUsage")
      val capabilities = variant.capabilitiesCollection.dependencies

      target.javaExtension.registerFeature(name) { spec ->
        spec.usingSourceSet(variant.prodSourceSet)

        for (capability in capabilities.get()) {
          @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
          spec.capability(capability.group, capability.name, capability.version)
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
        dokkaJavadocJar = target.tasks.dokkaJavadocJar
      )
    }
  }
}

private fun createPublication(
  target: Project,
  featureVariant: FeatureVariant,
  artifactId: String,
  publicationName: String,
  dokkaJavadocJar: Provider<out Jar>
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
