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

package com.rickbusarow.lattice.config.internal

import com.rickbusarow.lattice.config.LatticeProperties
import com.rickbusarow.lattice.config.LatticeProperties.JavaSettingsGroup
import com.rickbusarow.lattice.config.LatticeProperties.KotlinSettingsGroup
import com.rickbusarow.lattice.config.LatticeProperties.PublishingSettingsGroup
import com.rickbusarow.lattice.config.LatticeProperties.PublishingSettingsGroup.PomSettingsGroup
import com.rickbusarow.lattice.config.LatticeProperties.PublishingSettingsGroup.PomSettingsGroup.DeveloperSettingsGroup
import com.rickbusarow.lattice.config.LatticeProperties.PublishingSettingsGroup.PomSettingsGroup.LicenseSettingsGroup
import com.rickbusarow.lattice.config.LatticeProperties.PublishingSettingsGroup.PomSettingsGroup.ScmSettingsGroup
import com.rickbusarow.lattice.config.LatticeProperties.RepositorySettingsGroup
import com.rickbusarow.lattice.config.LatticeProperties.RepositorySettingsGroup.GithubSettingsGroup
import com.rickbusarow.lattice.config.LatticeProperties.VersionsGroup
import com.rickbusarow.lattice.deps.Versions
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.util.concurrent.Callable

internal class LatticePropertiesDefaults(
  private val target: Project
) : LatticeProperties {

  private val providers: ProviderFactory = target.providers

  private fun <T> provider(value: Callable<T?>): Provider<T> = providers.provider(value)
  private fun <T> nullValue(): Provider<T> = provider { null }

  override val group: Provider<String> = provider { target.group.toString() }
  override val versionName: Provider<String> = nullValue()
  override val versions: VersionsGroup = VersionsGroupDefault()

  internal inner class VersionsGroupDefault : VersionsGroup {
    override val detekt: Provider<String> = provider { Versions.detekt }
    override val dokka: Provider<String> = provider { Versions.dokka }
    override val kotlinxSerialization: Provider<String> =
      provider { Versions.`kotlinx-serialization` }
    override val ksp: Provider<String> = provider { Versions.ksp }
    override val poko: Provider<String> = provider { Versions.`drewHamilton-poko` }
  }

  override val kotlin: KotlinSettingsGroup = KotlinSettingsGroupDefault()

  internal inner class KotlinSettingsGroupDefault : KotlinSettingsGroup {
    override val compilerArgs: Provider<List<String>> = nullValue()
    override val apiLevel: Provider<String> = nullValue()
    override val allWarningsAsErrors: Provider<Boolean> = provider { false }
    override val explicitApi: Provider<Boolean> = provider { false }
  }

  override val java: JavaSettingsGroup = JavaSettingsGroupDefault()

  internal inner class JavaSettingsGroupDefault : JavaSettingsGroup {
    override val jvmTarget: Provider<String> = provider { "11" }
    override val jvmSource: Provider<String> = provider { "11" }
    override val jvmToolchain: Provider<String> = provider { "17" }
  }

  override val repository: RepositorySettingsGroup = RepositorySettingsGroupDefault()

  internal inner class RepositorySettingsGroupDefault : RepositorySettingsGroup {
    override val defaultBranch: Provider<String> = provider { "main" }
    override val github: GithubSettingsGroup = GithubSettingsGroupDefault()

    internal inner class GithubSettingsGroupDefault : GithubSettingsGroup {
      override val owner: Provider<String> = nullValue()
      override val repo: Provider<String> = nullValue()
    }
  }

  override val publishing: PublishingSettingsGroup = PublishingSettingsGroupDefault()

  internal inner class PublishingSettingsGroupDefault : PublishingSettingsGroup {
    override val pom: PomSettingsGroup = PomSettingsGroupDefault()

    internal inner class PomSettingsGroupDefault : PomSettingsGroup {
      override val name: Provider<String> = nullValue()
      override val description: Provider<String> = nullValue()
      override val url: Provider<String> = nullValue()
      override val inceptionYear: Provider<String> = nullValue()

      override val artifactId: Provider<String> = provider { target.name }

      override val license: LicenseSettingsGroup = LicenseSettingsGroupDefault()

      internal inner class LicenseSettingsGroupDefault : LicenseSettingsGroup {
        override val name: Provider<String> = nullValue()
        override val url: Provider<String> = nullValue()
        override val dist: Provider<String> = nullValue()
      }

      override val scm: ScmSettingsGroup = ScmSettingsGroupDefault()

      internal inner class ScmSettingsGroupDefault : ScmSettingsGroup {
        override val connection: Provider<String> = nullValue()
        override val url: Provider<String> = nullValue()
        override val devConnection: Provider<String> = nullValue()
      }

      override val developer: DeveloperSettingsGroup = DeveloperSettingsGroupDefault()

      internal inner class DeveloperSettingsGroupDefault : DeveloperSettingsGroup {
        override val id: Provider<String> = nullValue()
        override val name: Provider<String> = nullValue()
        override val url: Provider<String> = nullValue()
      }
    }
  }
}
