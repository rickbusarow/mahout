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

package com.rickbusarow.lattice.generator.utils

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import kotlin.LazyThreadSafetyMode.NONE

internal class Names {

  val javaSerializable = ClassName("java.io", "Serializable")

  val list by lazy(NONE) { List::class.asClassName() }
  val set by lazy(NONE) { Set::class.asClassName() }
  val boolean by lazy(NONE) { Boolean::class.asClassName() }
  val int by lazy(NONE) { Int::class.asClassName() }
  val string by lazy(NONE) { String::class.asClassName() }

  val gradleListProperty = ClassName("org.gradle.api.provider", "ListProperty")
  val gradleListPropertyString by lazy(NONE) {
    gradleListProperty.parameterizedBy(string)
  }
  val gradleProject = ClassName("org.gradle.api", "Project")
  val gradleProperty = ClassName("org.gradle.api.provider", "Property")
  val gradleProvider = ClassName("org.gradle.api.provider", "Provider")
  val gradleProviderBoolean by lazy(NONE) {
    gradleProvider.parameterizedBy(boolean)
  }
  val gradleObjectFactory = ClassName("org.gradle.api", "ObjectFactory")
  val gradleProviderFactory = ClassName("org.gradle.api.provider", "ProviderFactory")
  val gradleProviderInt by lazy(NONE) { gradleProvider.parameterizedBy(int) }
  val gradleSetProperty = ClassName("org.gradle.api.provider", "SetProperty")
  val javaxInject = ClassName("javax.inject", "Inject")
}
