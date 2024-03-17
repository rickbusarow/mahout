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

package com.rickbusarow.mahout.conventions

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import com.rickbusarow.kgx.applyOnce
import com.rickbusarow.kgx.property
import com.rickbusarow.kgx.withJavaGradlePluginPlugin
import com.rickbusarow.mahout.api.SubExtension
import com.rickbusarow.mahout.api.SubExtensionInternal
import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.core.InternalMahoutApi
import com.rickbusarow.mahout.deps.PluginIds
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.reflect.HasPublicType
import org.gradle.api.reflect.TypeOf
import org.gradle.internal.Actions
import javax.inject.Inject

/** */
public interface HasShadowSubExtension : java.io.Serializable {

  /** */
  public val shadow: ShadowSubExtension

  /** */
  public fun shadow(): Unit = shadow(Actions.doNothing())

  /** */
  public fun shadow(action: Action<in ShadowSubExtension>) {
    shadowInternal(
      shadowConfiguration = null,
      groupPrefix = null,
      relocatePackages = emptyList(),
      action = action
    )
  }

  /** */
  public fun shadow(
    shadowConfiguration: Configuration,
    action: Action<in ShadowSubExtension>
  ) {
    shadowInternal(
      shadowConfiguration = null,
      groupPrefix = null,
      relocatePackages = emptyList(),
      action = action
    )
  }

  /** */
  public fun shadow(
    groupPrefix: String,
    relocatePackages: List<String>,
    action: Action<in ShadowSubExtension>
  ) {
    shadowInternal(
      shadowConfiguration = null,
      groupPrefix = groupPrefix,
      relocatePackages = relocatePackages,
      action = action
    )
  }

  /** */
  public fun shadow(
    shadowConfiguration: Configuration,
    groupPrefix: String,
    relocatePackages: List<String>,
    action: Action<in ShadowSubExtension>
  ) {
    shadowInternal(
      shadowConfiguration = shadowConfiguration,
      groupPrefix = groupPrefix,
      relocatePackages = relocatePackages,
      action = action
    )
  }

  private fun shadowInternal(
    shadowConfiguration: Configuration?,
    groupPrefix: String?,
    relocatePackages: List<String>,
    action: Action<in ShadowSubExtension>
  ) {
    shadow.enabled.set(true)
    // shadow.enabled.finalizeValue()
    shadow.shadowConfiguration.set(shadowConfiguration)
    shadow.groupPrefix.set(groupPrefix)
    shadow.relocatePackages.set(relocatePackages)
    action.execute(shadow)
    (shadow as ShadowSubExtensionInternal).configureShadowJarTask()
  }
}

/** */
internal abstract class DefaultHasShadowSubExtension @Inject constructor(
  final override val objects: ObjectFactory
) : AbstractHasSubExtension(), HasShadowSubExtension {

  override val shadow: ShadowSubExtension by subExtension(DefaultShadowSubExtension::class)
}

/** */
public interface ShadowSubExtension : SubExtension<ShadowSubExtension> {

  /** */
  public val enabled: Property<Boolean>

  /** */
  public val shadowConfiguration: Property<Configuration>

  /** */
  public val groupPrefix: Property<String>

  /** */
  public val relocatePackages: ListProperty<String>
}

/** */
@InternalMahoutApi
public interface ShadowSubExtensionInternal : ShadowSubExtension {
  public fun configureShadowJarTask()
}

/** */
public abstract class DefaultShadowSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : AbstractSubExtension(target, objects),
  ShadowSubExtensionInternal,
  SubExtensionInternal,
  HasPublicType {

  override fun getPublicType(): TypeOf<*> = TypeOf.typeOf(GitHubSubExtension::class.java)

  override fun configureShadowJarTask() {

    target.plugins.applyOnce(PluginIds.`johnrengelman-shadow`)

    val classifier = objects.property(convention = "all")
    target.plugins.withJavaGradlePluginPlugin {
      classifier.set("")
    }

    val prefix = groupPrefix
      .orElse(target.mahoutProperties.group)
      .orElse(target.group.toString())

    val shadowJar = target.tasks.named("shadowJar", ShadowJar::class.java) { task ->

      val config = shadowConfiguration
        .orElse(target.configurations.getByName("shadow"))

      task.configurations = listOf(config.get())

      prefix.orNull?.let { groupStart ->

        val packages = relocatePackages.getOrElse(emptyList())

        for (realPackage in packages) {
          task.relocate(realPackage, "$groupStart.$realPackage")
        }
      }

      // task.relocationPrefix = prefix.orNull
      // task.isEnableRelocation = true

      task.archiveClassifier.set(classifier)

      task.transformers.add(ServiceFileTransformer())

      task.minimize()

      task.exclude(
        "**/*.kotlin_metadata",
        "**/*.kotlin_module",
        "META-INF/maven/**"
      )
    }

    // By adding the task's output to archives, it's automatically picked up by Gradle's maven-publish
    // plugin and added as an artifact to the publication.
    target.artifacts {
      it.add("runtimeOnly", shadowJar)
      it.add("archives", shadowJar)
    }
  }
}
