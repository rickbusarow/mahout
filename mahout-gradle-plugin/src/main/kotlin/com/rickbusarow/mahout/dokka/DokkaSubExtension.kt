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

package com.rickbusarow.mahout.dokka

import com.rickbusarow.kgx.property
import com.rickbusarow.mahout.api.SubExtension
import com.rickbusarow.mahout.api.SubExtensionInternal
import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.conventions.AbstractHasSubExtension
import com.rickbusarow.mahout.conventions.AbstractSubExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/** */
public interface HasDokkaSubExtension : java.io.Serializable {

  /** */
  public val dokka: DokkaSubExtension

  /** */
  public fun dokka(action: Action<in DokkaSubExtension>) {
    action.execute(dokka)
  }
}

internal abstract class DefaultHasDokkaSubExtension @Inject constructor(
  override val objects: ObjectFactory
) : AbstractHasSubExtension(), HasDokkaSubExtension {

  override val dokka: DokkaSubExtension by subExtension(DefaultDokkaSubExtension::class)
}

/** */
public interface DokkaSubExtension : SubExtension<DokkaSubExtension> {

  /** */
  public val dokkaVersion: Property<String>
}

/** */
public abstract class DefaultDokkaSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : AbstractSubExtension(target, objects),
  DokkaSubExtension,
  SubExtensionInternal {

  override val dokkaVersion: Property<String> = property(target.mahoutProperties.versions.dokka)
}
