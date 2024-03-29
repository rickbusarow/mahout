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

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.plugin.devel.PluginDeclaration
import javax.inject.Inject

/** */
public interface PublishingGradlePluginHandler : java.io.Serializable {

  /** */
  public fun publishPlugin(pluginDeclaration: NamedDomainObjectProvider<PluginDeclaration>)
}

/** */
public open class DefaultPublishingGradlePluginHandler @Inject constructor() :
  PublishingGradlePluginHandler {

  override fun publishPlugin(pluginDeclaration: NamedDomainObjectProvider<PluginDeclaration>) {
    TODO()
  }
}
