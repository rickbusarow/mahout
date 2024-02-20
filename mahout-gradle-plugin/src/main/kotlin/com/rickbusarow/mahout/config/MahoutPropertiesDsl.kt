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

@file:Suppress("PackageDirectoryMismatch")

package org.gradle.kotlin.dsl

import com.rickbusarow.mahout.config.JavaVersion
import com.rickbusarow.mahout.config.MahoutProperties
import org.gradle.api.Project
import org.gradle.api.provider.Property
import com.rickbusarow.mahout.config.mahoutProperties as mahoutPropertiesInternal

/** */
public fun Property<JavaVersion>.set(value: String) {
  set(JavaVersion(value))
}

/** */
public fun Property<JavaVersion>.set(value: Int) {
  @Suppress("MagicNumber")
  when {
    value < 9 -> set(JavaVersion("1.$value"))
    else -> set(JavaVersion(value.toString()))
  }
}

/** */
public val Project.mahoutProperties: MahoutProperties
  get() = mahoutPropertiesInternal
