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

package com.rickbusarow.mahout.publishing

import com.rickbusarow.mahout.api.SubExtensionInternal
import com.rickbusarow.mahout.conventions.AbstractSubExtension
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.plugin.devel.PluginDeclaration
import javax.inject.Inject

/** */
public interface HasPublishingGradlePluginSubExtension : java.io.Serializable {

  /** */
  public fun publishPlugin(pluginDeclaration: NamedDomainObjectProvider<PluginDeclaration>)
}

/** */
public abstract class DefaultHasPublishingGradlePluginSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : AbstractSubExtension(target, objects),
  HasPublishingGradlePluginSubExtension,
  SubExtensionInternal {

  override fun publishPlugin(pluginDeclaration: NamedDomainObjectProvider<PluginDeclaration>) {

    target.plugins.withId("com.gradle.plugin-publish") {

      pluginDeclaration.configure { declaration ->

        requireNotNull(declaration.description) { "A plugin description is required." }
      }

      target.mavenPublications.configureEach { publication ->

        publication.pom {
          // it.name.set(pomName)
          it.description.set(pluginDeclaration.map { it.description })
        }
      }
    }
  }
}
