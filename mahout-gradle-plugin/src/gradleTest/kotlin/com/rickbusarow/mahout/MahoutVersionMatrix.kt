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

import com.rickbusarow.kase.KaseMatrix
import com.rickbusarow.kase.gradle.AgpDependencyVersion
import com.rickbusarow.kase.gradle.DaggerDependencyVersion
import com.rickbusarow.kase.gradle.GradleDependencyVersion
import com.rickbusarow.kase.gradle.KotlinDependencyVersion
import org.gradle.util.GradleVersion

class MahoutVersionMatrix(
  agp: List<AgpDependencyVersion> = agpList,
  kotlin: List<KotlinDependencyVersion> = kotlinList,
  gradle: List<GradleDependencyVersion> = gradleList,
  dagger: List<DaggerDependencyVersion> = daggerList
) : KaseMatrix by KaseMatrix(agp + kotlin + gradle + dagger) {
  private companion object {
    val agpList =
      setOf("8.1.1", "8.2.0").map(::AgpDependencyVersion).sorted()
    val kotlinList =
      setOf(KotlinVersion.CURRENT.toString())
        .map(::KotlinDependencyVersion).sorted()
    val gradleList =
      setOf(GradleVersion.current().version)
        .map(::GradleDependencyVersion).sorted()
    val daggerList = setOf("2.50").map(::DaggerDependencyVersion)
  }
}
