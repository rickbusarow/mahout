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

import com.rickbusarow.kgx.listPropertyLazy
import com.rickbusarow.kgx.property
import com.rickbusarow.mahout.api.SubExtension
import com.rickbusarow.mahout.api.SubExtensionInternal
import com.rickbusarow.mahout.config.JavaVersion
import com.rickbusarow.mahout.config.MahoutProperties.JavaSettingsGroup
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/** */
public interface HasJavaSubExtension : java.io.Serializable {

  /** */
  public val java: JavaSubExtension

  /** */
  public fun java(action: Action<in JavaSubExtension>) {
    action.execute(java)
  }
}

internal abstract class DefaultHasJavaSubExtension @Inject constructor(
  final override val objects: ObjectFactory
) : AbstractHasSubExtension(), HasJavaSubExtension {

  override val java: JavaSubExtension by subExtension(DefaultJavaSubExtension::class)
}

/** */
public interface JavaSubExtension : SubExtension<JavaSubExtension>, JavaSettingsGroup {

  override val jvmSource: Property<JavaVersion>
  override val jvmTarget: Property<JavaVersion>
  override val jvmToolchain: Property<JavaVersion>
  override val testJvmTargets: ListProperty<JavaVersion>
}

/** */
public abstract class DefaultJavaSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : AbstractSubExtension(target, objects),
  JavaSubExtension,
  SubExtensionInternal {

  final override val jvmTarget: Property<JavaVersion> =
    objects.property(mahoutProperties.java.jvmTarget)

  final override val jvmSource: Property<JavaVersion> =
    objects.property(mahoutProperties.java.jvmSource)

  final override val jvmToolchain: Property<JavaVersion> =
    objects.property(mahoutProperties.java.jvmToolchain)

  final override val testJvmTargets: ListProperty<JavaVersion> by objects.listPropertyLazy()
}
