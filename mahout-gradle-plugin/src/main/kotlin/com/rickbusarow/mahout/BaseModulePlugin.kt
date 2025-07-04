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

import com.rickbusarow.kgx.isRootProject
import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.conventions.BenManesVersionsPlugin
import com.rickbusarow.mahout.conventions.CleanPlugin
import com.rickbusarow.mahout.conventions.DependencyGuardConventionPlugin
import com.rickbusarow.mahout.conventions.DetektConventionPlugin
import com.rickbusarow.mahout.conventions.DokkaVersionArchivePlugin
import com.rickbusarow.mahout.conventions.FixPlugin
import com.rickbusarow.mahout.conventions.GitHubReleasePlugin
import com.rickbusarow.mahout.conventions.JdkVersionsConventionPlugin
import com.rickbusarow.mahout.conventions.KotlinJvmConventionPlugin
import com.rickbusarow.mahout.conventions.KotlinMultiplatformConventionPlugin
import com.rickbusarow.mahout.conventions.KtLintConventionPlugin
import com.rickbusarow.mahout.conventions.SpotlessConventionPlugin
import com.rickbusarow.mahout.conventions.TestConventionPlugin
import com.rickbusarow.mahout.curator.CuratorPlugin
import com.rickbusarow.mahout.dokka.DokkaConventionPlugin
import com.rickbusarow.mahout.publishing.MahoutPublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin

/** Applies common conventions to any project. */
public abstract class BaseModulePlugin : Plugin<Project> {
  override fun apply(target: Project) {

    target.mahoutProperties.group.orNull?.let { target.group = it }
    target.mahoutProperties.versionName.orNull?.let { target.version = it }

    target.plugins.apply(JavaBasePlugin::class.java)
    target.plugins.apply(MahoutPublishPlugin::class.java)
    target.plugins.apply(JdkVersionsConventionPlugin::class.java)
    target.plugins.apply(FixPlugin::class.java)
    target.plugins.apply(CleanPlugin::class.java)
    target.plugins.apply(DependencyGuardConventionPlugin::class.java)
    target.plugins.apply(DetektConventionPlugin::class.java)
    target.plugins.apply(DokkaConventionPlugin::class.java)
    target.plugins.apply(KtLintConventionPlugin::class.java)
    target.plugins.apply(TestConventionPlugin::class.java)

    if (target.isRootProject()) {
      applyRootConventionPlugins(target)
    }
  }

  protected fun applyRootConventionPlugins(target: Project) {

    target.plugins.apply(CuratorPlugin::class.java)
    target.plugins.apply(BenManesVersionsPlugin::class.java)
    target.plugins.apply(DokkaVersionArchivePlugin::class.java)
    target.plugins.apply(GitHubReleasePlugin::class.java)
    target.plugins.apply(SpotlessConventionPlugin::class.java)
  }
}

/** Applies conventions to any kotlin-jvm and `java-gradle-plugin` project. */
public abstract class GradlePluginModulePlugin : BaseModulePlugin() {
  override fun apply(target: Project) {

    target.extensions.create("mahout", GradlePluginModuleExtension::class.java)

    target.plugins.apply(KotlinJvmConventionPlugin::class.java)

    super.apply(target)
  }
}

/** Applies conventions to any kotlin-jvm project. */
public abstract class KotlinJvmModulePlugin : BaseModulePlugin() {
  override fun apply(target: Project) {

    target.extensions.create("mahout", KotlinJvmModuleExtension::class.java)

    target.plugins.apply(KotlinJvmConventionPlugin::class.java)

    super.apply(target)
  }
}

/** Applies conventions to any kotlin-multiplatform project. */
public abstract class KotlinMultiplatformModulePlugin : BaseModulePlugin() {
  override fun apply(target: Project) {

    target.extensions.create("mahout", KotlinMultiplatformModuleExtension::class.java)

    target.plugins.apply(KotlinMultiplatformConventionPlugin::class.java)

    super.apply(target)
  }
}
