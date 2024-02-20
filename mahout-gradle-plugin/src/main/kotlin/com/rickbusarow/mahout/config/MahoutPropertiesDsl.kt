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

import com.rickbusarow.mahout.config.JvmVersion
import org.gradle.api.provider.Property

/** */
public fun Property<JvmVersion>.set(value: String) {
  set(JvmVersion(value))
}

/** */
public fun Property<JvmVersion>.set(value: Int) {
  @Suppress("MagicNumber")
  when {
    value < 9 -> set(JvmVersion("1.$value"))
    else -> set(JvmVersion(value.toString()))
  }
}
