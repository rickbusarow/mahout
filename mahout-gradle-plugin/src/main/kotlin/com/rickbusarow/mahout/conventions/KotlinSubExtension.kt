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

import com.rickbusarow.kgx.property
import com.rickbusarow.mahout.api.SubExtension
import com.rickbusarow.mahout.api.SubExtensionInternal
import com.rickbusarow.mahout.config.MahoutProperties.KotlinSettingsGroup
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/** */
public interface HasKotlinSubExtension : java.io.Serializable {

  /** */
  public val kotlin: KotlinSubExtension

  /** */
  public fun kotlin(action: Action<in KotlinSubExtension>) {
    action.execute(kotlin)
  }
}

/** */
public interface HasKotlinJvmSubExtension : HasKotlinSubExtension {
  public override val kotlin: KotlinJvmSubExtension
}

/** */
public interface HasKotlinMultiplatformSubExtension : HasKotlinSubExtension {
  public override val kotlin: KotlinMultiplatformSubExtension
}

internal abstract class DefaultHasKotlinSubExtension @Inject constructor(
  final override val objects: ObjectFactory
) : AbstractHasSubExtension(), HasKotlinSubExtension {

  override val kotlin: KotlinSubExtension by subExtension(DefaultKotlinSubExtension::class)
}

internal abstract class DefaultHasKotlinJvmSubExtension @Inject constructor(
  objects: ObjectFactory
) : DefaultHasKotlinSubExtension(objects), HasKotlinJvmSubExtension {

  override val kotlin: KotlinJvmSubExtension by subExtension(DefaultKotlinJvmSubExtension::class)
}

internal abstract class DefaultHasKotlinMultiplatformSubExtension @Inject constructor(
  objects: ObjectFactory
) : DefaultHasKotlinSubExtension(objects),
  HasKotlinMultiplatformSubExtension {

  override val kotlin: KotlinMultiplatformSubExtension
    by subExtension(DefaultKotlinMultiplatformSubExtension::class)
}

/** */
public interface KotlinSubExtension : SubExtension<KotlinSubExtension>, KotlinSettingsGroup {

  override val apiLevel: Property<String>
  override val allWarningsAsErrors: Property<Boolean>
  override val explicitApi: Property<Boolean>
  override val compilerArgs: ListProperty<String>
}

/** */
public abstract class DefaultKotlinSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : AbstractSubExtension(target, objects), KotlinSubExtension, SubExtensionInternal {

  override val apiLevel: Property<String> =
    objects.property(mahoutProperties.kotlin.apiLevel)

  override val allWarningsAsErrors: Property<Boolean> =
    objects.property(mahoutProperties.kotlin.allWarningsAsErrors)

  override val explicitApi: Property<Boolean> =
    objects.property(mahoutProperties.kotlin.explicitApi)
}

/** */
public interface KotlinJvmSubExtension : KotlinSubExtension

/** */
public abstract class DefaultKotlinJvmSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : DefaultKotlinSubExtension(target, objects),
  KotlinJvmSubExtension,
  SubExtensionInternal

/** */
public interface KotlinMultiplatformSubExtension : KotlinSubExtension

/** */
public abstract class DefaultKotlinMultiplatformSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : DefaultKotlinSubExtension(target, objects),
  KotlinMultiplatformSubExtension,
  SubExtensionInternal
