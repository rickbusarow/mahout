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

package com.rickbusarow.mahout.publishing

import com.rickbusarow.kgx.isRootProject
import com.rickbusarow.mahout.api.SubExtension
import com.rickbusarow.mahout.api.SubExtensionInternal
import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.conventions.AbstractHasSubExtension
import com.rickbusarow.mahout.conventions.applyBinaryCompatibility
import com.rickbusarow.mahout.core.setIfNull
import com.rickbusarow.mahout.deps.PluginIds
import com.rickbusarow.mahout.dokka.DokkatooConventionPlugin
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.PluginDeclaration
import javax.inject.Inject

/** */
public interface HasPublishingGradlePluginSubExtension : java.io.Serializable {

  /** */
  public val publishing: PublishingGradlePluginSubExtension

  /** */
  public fun publishing(action: Action<in PublishingGradlePluginSubExtension>) {
    action.execute(publishing)
  }
}

internal abstract class DefaultHasPublishingGradlePluginSubExtension @Inject constructor(
  override val objects: ObjectFactory
) : AbstractHasSubExtension(),
  HasPublishingGradlePluginSubExtension,
  SubExtensionInternal {

  override val publishing: PublishingGradlePluginSubExtension by
    subExtension(DefaultPublishingGradlePluginSubExtension::class)
}

/** */
public interface PublishingGradlePluginSubExtension :
  SubExtension<PublishingGradlePluginSubExtension>,
  HasPublishMaven {

  /** */
  public fun pluginMaven(
    groupId: String? = null,
    artifactId: String? = null,
    pomDescription: String? = null
  )

  /** */
  public fun publishPlugin(pluginDeclaration: NamedDomainObjectProvider<PluginDeclaration>)
}

/** */
public abstract class DefaultPublishingGradlePluginSubExtension @Inject constructor(
  target: Project
) : DefaultHasPublishMaven(target, target.objects),
  PublishingGradlePluginSubExtension,
  SubExtensionInternal {

  private var setPomDescription: Boolean = false

  override fun pluginMaven(
    groupId: String?,
    artifactId: String?,
    pomDescription: String?
  ) {

    configurePluginMaven { publication ->

      publication.groupId = groupId
        ?: target.mahoutProperties.group.orNull
        ?: target.group.toString()
      publication.artifactId =
        artifactId ?: target.mahoutProperties.publishing.pom.artifactId.orNull

      publication.pom.description.set(
        target.provider {
          pomDescription ?: target.mahoutProperties.publishing.pom.description.orNull
        }
      )
    }
  }

  private fun configurePluginMaven(action: Action<in MavenPublication>) {

    target.gradlePublishingExtension.publications
      .withType(MavenPublication::class.java)
      .named { it == "pluginMaven" }
      .configureEach(action)
  }

  override fun publishPlugin(
    pluginDeclaration: NamedDomainObjectProvider<PluginDeclaration>
  ) {

    target.plugins.apply("java-gradle-plugin")
    target.plugins.apply(PluginIds.`plugin-publish`)
    target.plugins.apply(DokkatooConventionPlugin::class.java)
    target.plugins.apply("maven-publish")

    configurePublishPlugin()

    if (!setPomDescription) {

      configurePluginMaven { publication ->

        publication.pom.description.setIfNull(
          defaultPom.description.orElse(pluginDeclaration.map { it.description })
        )
        if (publication.groupId == null) {
          publication.groupId = target.mahoutProperties.group.orNull ?: target.group.toString()
        }
      }

      setPomDescription = true
    }
  }

  private fun configurePublishPlugin() {
    if (target.isRootProject()) {
      target.applyBinaryCompatibility()
    }

    target.plugins.withId(PluginIds.`plugin-publish`) {

      target.extensions.configure(
        GradlePluginDevelopmentExtension::class.java
      ) { pluginDevelopmentExtension ->

        pluginDevelopmentExtension.website.set(target.mahoutProperties.publishing.pom.url)
        pluginDevelopmentExtension.vcsUrl.set(target.mahoutProperties.publishing.pom.scm.url)
      }
    }
  }
}
