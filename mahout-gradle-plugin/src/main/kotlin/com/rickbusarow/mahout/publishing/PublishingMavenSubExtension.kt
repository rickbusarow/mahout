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

import com.rickbusarow.mahout.api.SubExtension
import com.rickbusarow.mahout.api.SubExtensionInternal
import com.rickbusarow.mahout.conventions.AbstractHasSubExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/** */
public interface HasPublishingMavenSubExtension : java.io.Serializable {

  /** */
  public val publishing: PublishingMavenSubExtension

  /** */
  public fun publishing(action: Action<in PublishingMavenSubExtension>) {
    action.execute(publishing)
  }
}

internal abstract class DefaultHasPublishingMavenSubExtension @Inject constructor(
  override val objects: ObjectFactory
) : AbstractHasSubExtension(),
  HasPublishingMavenSubExtension,
  SubExtensionInternal {

  override val publishing: PublishingMavenSubExtension by
    subExtension(DefaultPublishingMavenSubExtension::class)
}

/** */
public interface PublishingMavenSubExtension :
  SubExtension<PublishingMavenSubExtension>,
  HasPublishMaven

/** */
public abstract class DefaultPublishingMavenSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : DefaultHasPublishMaven(target, objects),
  PublishingMavenSubExtension,
  SubExtensionInternal
