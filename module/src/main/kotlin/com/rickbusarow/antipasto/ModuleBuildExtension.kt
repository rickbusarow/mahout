/*
 * Copyright (C) 2023 Rick Busarow
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

package com.rickbusarow.antipasto

import com.rickbusarow.antipasto.conventions.AutoServiceExtension
import com.rickbusarow.antipasto.conventions.BuildLogicShadowExtensionHook
import com.rickbusarow.antipasto.conventions.KotlinExtension
import com.rickbusarow.antipasto.conventions.KotlinJvmExtension
import com.rickbusarow.antipasto.conventions.KotlinMultiplatformExtension
import com.rickbusarow.antipasto.conventions.KspExtension
import com.rickbusarow.antipasto.conventions.PokoExtension
import com.rickbusarow.antipasto.conventions.PublishingExtension
import com.rickbusarow.antipasto.conventions.SerializationExtension

public abstract class RootExtension :
  AutoServiceExtension,
  BuildLogicShadowExtensionHook,
  KspExtension,
  PokoExtension,
  PublishingExtension,
  SerializationExtension

public abstract class KotlinJvmModuleExtension :
  AutoServiceExtension,
  BuildLogicShadowExtensionHook,
  KotlinJvmExtension,
  KspExtension,
  PokoExtension,
  PublishingExtension,
  SerializationExtension

public abstract class KotlinMultiplatformModuleExtension :
  AutoServiceExtension,
  BuildLogicShadowExtensionHook,
  KotlinExtension,
  KotlinMultiplatformExtension,
  KspExtension,
  PokoExtension,
  PublishingExtension,
  SerializationExtension
