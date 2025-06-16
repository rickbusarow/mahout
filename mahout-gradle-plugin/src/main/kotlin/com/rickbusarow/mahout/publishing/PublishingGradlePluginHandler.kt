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
import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.conventions.applyBinaryCompatibility
import com.rickbusarow.mahout.dokka.DokkatooConventionPlugin
import modulecheck.utils.requireNotNull
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.PluginDeclaration
import javax.inject.Inject

/** */
public interface PublishingGradlePluginHandler : java.io.Serializable {

  /** */
  public fun publishPlugin(pluginDeclaration: NamedDomainObjectProvider<PluginDeclaration>)
}

/** */
public open class DefaultPublishingGradlePluginHandler @Inject constructor(
  private val target: Project
) : PublishingGradlePluginHandler {

  override fun publishPlugin(
    pluginDeclaration: NamedDomainObjectProvider<PluginDeclaration>
  ) {

    target.plugins.apply("java-gradle-plugin")
    target.plugins.apply("com.gradle.plugin-publish")
    target.plugins.apply("com.vanniktech.maven.publish.base")
    target.plugins.apply(DokkatooConventionPlugin::class.java)

    require(target.pluginManager.hasPlugin("org.jetbrains.kotlin.jvm"))

    configurePublishPlugin(pluginDeclaration)
  }

  private fun configurePublishPlugin(
    pluginDeclaration: NamedDomainObjectProvider<PluginDeclaration>
  ) {
    if (target.isRootProject()) {
      target.applyBinaryCompatibility()
    }

    target.plugins.withId("com.gradle.plugin-publish") {

      target.extensions.configure(
        GradlePluginDevelopmentExtension::class.java
      ) { pluginDevelopmentExtension ->

        pluginDevelopmentExtension.website.set(target.mahoutProperties.publishing.pom.url)
        pluginDevelopmentExtension.vcsUrl.set(target.mahoutProperties.publishing.pom.scm.url)
      }
    }

    target.gradlePublishingExtension.publications
      .withType(MavenPublication::class.java)
      .configureEach { publication ->

        publication.pom.description.set(
          pluginDeclaration.map {
            it.description.requireNotNull { "A plugin description is required." }
          }
        )
      }
  }
}
