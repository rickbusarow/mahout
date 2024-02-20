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

package com.rickbusarow.mahout

import com.rickbusarow.mahout.composite.CompositeHandler
import com.rickbusarow.mahout.composite.DefaultCompositeHandler
import com.rickbusarow.mahout.conventions.AutoServiceExtension
import com.rickbusarow.mahout.conventions.BuildLogicShadowExtensionHook
import com.rickbusarow.mahout.conventions.KotlinExtension
import com.rickbusarow.mahout.conventions.KotlinJvmExtension
import com.rickbusarow.mahout.conventions.KotlinMultiplatformExtension
import com.rickbusarow.mahout.conventions.KspExtension
import com.rickbusarow.mahout.conventions.PokoExtension
import com.rickbusarow.mahout.conventions.SerializationExtension
import com.rickbusarow.mahout.publishing.PublishingExtension
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

@Suppress("UndocumentedPublicClass")
public abstract class RootExtension @Inject constructor(
  private val objects: ObjectFactory
) : CompositeHandler by objects<DefaultCompositeHandler>(),
  AutoServiceExtension,
  BuildLogicShadowExtensionHook,
  KspExtension,
  PokoExtension,
  PublishingExtension,
  SerializationExtension

@Suppress("UndocumentedPublicClass")
public abstract class KotlinJvmModuleExtension :
  AutoServiceExtension,
  BuildLogicShadowExtensionHook,
  KotlinJvmExtension,
  KspExtension,
  PokoExtension,
  PublishingExtension,
  SerializationExtension

@Suppress("UndocumentedPublicClass")
public abstract class KotlinMultiplatformModuleExtension :
  AutoServiceExtension,
  BuildLogicShadowExtensionHook,
  KotlinExtension,
  KotlinMultiplatformExtension,
  KspExtension,
  PokoExtension,
  PublishingExtension,
  SerializationExtension

@Suppress("UnusedPrivateMember") // no, it's used as a delegate
private inline operator fun <reified T : Any> ObjectFactory.invoke(): T = newInstance(T::class.java)
