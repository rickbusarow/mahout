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

package com.rickbusarow.mahout

import com.rickbusarow.kgx.newInstance
import com.rickbusarow.mahout.api.MahoutDsl
import com.rickbusarow.mahout.composite.DefaultHasCompositeSubExtension
import com.rickbusarow.mahout.composite.HasCompositeSubExtension
import com.rickbusarow.mahout.conventions.AutoServiceExtension
import com.rickbusarow.mahout.conventions.BuildLogicShadowExtensionHook
import com.rickbusarow.mahout.conventions.DefaultHasGitHubSubExtension
import com.rickbusarow.mahout.conventions.DefaultHasGradleTestsSubExtension
import com.rickbusarow.mahout.conventions.DefaultHasJavaSubExtension
import com.rickbusarow.mahout.conventions.DefaultHasKotlinJvmSubExtension
import com.rickbusarow.mahout.conventions.DefaultHasKotlinSubExtension
import com.rickbusarow.mahout.conventions.HasGitHubSubExtension
import com.rickbusarow.mahout.conventions.HasGradleTestsSubExtension
import com.rickbusarow.mahout.conventions.HasJavaSubExtension
import com.rickbusarow.mahout.conventions.HasKotlinJvmSubExtension
import com.rickbusarow.mahout.conventions.HasKotlinSubExtension
import com.rickbusarow.mahout.conventions.KotlinExtension
import com.rickbusarow.mahout.conventions.KotlinMultiplatformExtension
import com.rickbusarow.mahout.conventions.KspExtension
import com.rickbusarow.mahout.conventions.PokoExtension
import com.rickbusarow.mahout.conventions.SerializationExtension
import com.rickbusarow.mahout.dokka.DefaultHasDokkaSubExtension
import com.rickbusarow.mahout.dokka.HasDokkaSubExtension
import com.rickbusarow.mahout.publishing.DefaultHasPublishingGradlePluginSubExtension
import com.rickbusarow.mahout.publishing.DefaultHasPublishingMavenSubExtension
import com.rickbusarow.mahout.publishing.HasPublishingGradlePluginSubExtension
import com.rickbusarow.mahout.publishing.HasPublishingMavenSubExtension
import com.rickbusarow.mahout.variants.DefaultHasFeatureVariantsSubExtension
import com.rickbusarow.mahout.variants.HasFeatureVariantsSubExtension
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import javax.inject.Inject

/** */
public interface MahoutExtensionInternal : ExtensionAware {

  /** */
  public val objects: ObjectFactory

  /** */
  public val target: Project
}

internal val Project.mahoutExtension: BaseMahoutExtension
  get() = extensions.getByType(BaseMahoutExtension::class.java)

internal inline fun <reified T> Project.mahoutExtensionAs(): T = mahoutExtension as T

/** */
@MahoutDsl
public abstract class BaseMahoutExtension @Inject constructor(
  private val target: Project,
  private val objects: ObjectFactory
) : ExtensionAware,
  CoreMahoutProperties by objects.newInstance<DefaultCoreMahoutProperties>()

/** */
public abstract class RootExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : BaseMahoutExtension(target, objects),
  HasCompositeSubExtension by objects.newInstance<DefaultHasCompositeSubExtension>(),
  HasDokkaSubExtension by objects.newInstance<DefaultHasDokkaSubExtension>(),
  HasGitHubSubExtension by objects.newInstance<DefaultHasGitHubSubExtension>(),
  HasJavaSubExtension by objects.newInstance<DefaultHasJavaSubExtension>(),
  HasKotlinSubExtension by objects.newInstance<DefaultHasKotlinSubExtension>(),
  HasTasksSubExtension by objects.newInstance<DefaultHasTasksSubExtension>(),
  AutoServiceExtension,
  BuildLogicShadowExtensionHook,
  KspExtension,
  PokoExtension,
  SerializationExtension

/** */
public abstract class GradlePluginModuleExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : BaseMahoutExtension(target, objects),
  HasDokkaSubExtension by objects.newInstance<DefaultHasDokkaSubExtension>(),
  HasFeatureVariantsSubExtension by objects.newInstance<DefaultHasFeatureVariantsSubExtension>(),
  HasGitHubSubExtension by objects.newInstance<DefaultHasGitHubSubExtension>(),
  HasGradleTestsSubExtension by objects.newInstance<DefaultHasGradleTestsSubExtension>(),
  HasJavaSubExtension by objects.newInstance<DefaultHasJavaSubExtension>(),
  HasKotlinJvmSubExtension by objects.newInstance<DefaultHasKotlinJvmSubExtension>(),
  HasPublishingGradlePluginSubExtension by objects.newInstance<DefaultHasPublishingGradlePluginSubExtension>(),
  HasTasksSubExtension by objects.newInstance<DefaultHasTasksSubExtension>(),
  AutoServiceExtension,
  BuildLogicShadowExtensionHook,
  KspExtension,
  PokoExtension,
  SerializationExtension

/** */
public abstract class KotlinJvmModuleExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : BaseMahoutExtension(target, objects),
  HasDokkaSubExtension by objects.newInstance<DefaultHasDokkaSubExtension>(),
  HasFeatureVariantsSubExtension by objects.newInstance<DefaultHasFeatureVariantsSubExtension>(),
  HasGitHubSubExtension by objects.newInstance<DefaultHasGitHubSubExtension>(),
  HasGradleTestsSubExtension by objects.newInstance<DefaultHasGradleTestsSubExtension>(),
  HasJavaSubExtension by objects.newInstance<DefaultHasJavaSubExtension>(),
  HasKotlinJvmSubExtension by objects.newInstance<DefaultHasKotlinJvmSubExtension>(),
  HasPublishingMavenSubExtension by objects.newInstance<DefaultHasPublishingMavenSubExtension>(),
  HasTasksSubExtension by objects.newInstance<DefaultHasTasksSubExtension>(),
  AutoServiceExtension,
  BuildLogicShadowExtensionHook,
  KspExtension,
  PokoExtension,
  SerializationExtension

/** */
public abstract class KotlinMultiplatformModuleExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : BaseMahoutExtension(target, objects),
  AutoServiceExtension,
  BuildLogicShadowExtensionHook,
  KotlinExtension,
  KotlinMultiplatformExtension,
  KspExtension,
  PokoExtension,
  SerializationExtension
