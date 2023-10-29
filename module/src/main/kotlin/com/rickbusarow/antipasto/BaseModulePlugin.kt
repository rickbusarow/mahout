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

import com.rickbusarow.antipasto.conventions.CheckPlugin
import com.rickbusarow.antipasto.conventions.CleanPlugin
import com.rickbusarow.antipasto.conventions.DependencyGuardConventionPlugin
import com.rickbusarow.antipasto.conventions.DetektConventionPlugin
import com.rickbusarow.antipasto.conventions.DokkatooConventionPlugin
import com.rickbusarow.antipasto.conventions.KotlinJvmConventionPlugin
import com.rickbusarow.antipasto.conventions.KotlinMultiplatformConventionPlugin
import com.rickbusarow.antipasto.conventions.KtLintConventionPlugin
import com.rickbusarow.antipasto.conventions.TestConventionPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Applies common conventions to any project. */
public abstract class BaseModulePlugin : Plugin<Project> {
  override fun apply(target: Project) {

    target.plugins.apply(CheckPlugin::class.java)
    target.plugins.apply(CleanPlugin::class.java)
    target.plugins.apply(DependencyGuardConventionPlugin::class.java)
    target.plugins.apply(DetektConventionPlugin::class.java)
    target.plugins.apply(DokkatooConventionPlugin::class.java)
    target.plugins.apply(KtLintConventionPlugin::class.java)
    target.plugins.apply(TestConventionPlugin::class.java)
  }
}

/** Applies conventions to any kotlin-jvm project. */
public abstract class KotlinJvmModulePlugin : BaseModulePlugin() {
  override fun apply(target: Project) {

    target.extensions.create("jvmModule", KotlinJvmModuleExtension::class.java)

    target.plugins.apply(KotlinJvmConventionPlugin::class.java)

    super.apply(target)
  }
}

/** Applies conventions to any kotlin-multiplatform project. */
public abstract class KotlinMultiplatformModulePlugin : BaseModulePlugin() {
  override fun apply(target: Project) {

    target.extensions.create("kmpModule", KotlinMultiplatformModuleExtension::class.java)

    target.plugins.apply(KotlinMultiplatformConventionPlugin::class.java)

    super.apply(target)
  }
}
