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

package com.rickbusarow.lattice

import com.rickbusarow.kgx.checkProjectIsRoot
import com.rickbusarow.kgx.inCI
import com.rickbusarow.kgx.isRealRootProject
import com.rickbusarow.lattice.conventions.BenManesVersionsPlugin
import com.rickbusarow.lattice.conventions.DokkaVersionArchivePlugin
import com.rickbusarow.lattice.conventions.GitHubReleasePlugin
import com.rickbusarow.lattice.conventions.SpotlessConventionPlugin
import com.rickbusarow.lattice.curator.CuratorPlugin
import modulecheck.gradle.ModuleCheckExtension
import modulecheck.gradle.ModuleCheckPlugin
import org.gradle.api.Project

/** Applied to the real project root and the root project of any included build except this one. */
public abstract class RootPlugin : BaseModulePlugin() {
  override fun apply(target: Project) {

    target.checkProjectIsRoot()

    target.extensions.create("lattice", RootExtension::class.java)

    super.apply(target)

    target.plugins.apply(CuratorPlugin::class.java)
    target.plugins.apply(BenManesVersionsPlugin::class.java)
    target.plugins.apply(DokkaVersionArchivePlugin::class.java)
    target.plugins.apply(GitHubReleasePlugin::class.java)
    target.plugins.apply(SpotlessConventionPlugin::class.java)

    target.plugins.apply(ModuleCheckPlugin::class.java)

    target.extensions.configure(ModuleCheckExtension::class.java) { extension ->
      extension.deleteUnused = true
      extension.checks { checks ->
        checks.sortDependencies = true
      }
    }

    if (target.gradle.includedBuilds.isNotEmpty()) {
      target.plugins.apply(CompositePlugin::class.java)
    }

    if (inCI() && target.isRealRootProject()) {
      target.logger.lifecycle("CI environment detected.")
    }
  }
}
