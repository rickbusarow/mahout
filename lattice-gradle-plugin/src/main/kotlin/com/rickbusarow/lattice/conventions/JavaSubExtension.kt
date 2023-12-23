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

package com.rickbusarow.lattice.conventions

import com.rickbusarow.lattice.core.SubExtension
import com.rickbusarow.lattice.core.SubExtensionInternal
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import javax.inject.Inject

public interface HasJavaSubExtension : java.io.Serializable {
  public val java: JavaSubExtension
  public fun java(action: Action<in JavaSubExtension>) {
    action.execute(java)
  }
}

internal abstract class DefaultHasJavaSubExtension @Inject constructor(
  final override val objects: ObjectFactory
) : AbstractHasSubExtension(), HasJavaSubExtension {

  override val java: JavaSubExtension by subExtension(DefaultJavaSubExtension::class)
}

public interface JavaSubExtension : SubExtension<JavaSubExtension> {
  public val jvmTarget: Property<String>
  public val jvmTargetInt: Provider<Int>

  public val jvmSource: Property<String>
  public val jvmSourceInt: Provider<Int>

  public val jvmToolchain: Property<String>
  public val jvmToolchainInt: Provider<Int>
}

public abstract class DefaultJavaSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : AbstractSubExtension(target, objects),
  JavaSubExtension,
  SubExtensionInternal {

  final override val jvmTarget: Property<String> = objects.property(String::class.java)
    .convention(latticeProperties.java.jvmTarget)
  override val jvmTargetInt: Provider<Int> = jvmTarget.map { it.substringAfterLast('.').toInt() }

  final override val jvmSource: Property<String> = objects.property(String::class.java)
    .convention(latticeProperties.java.jvmSource)
  override val jvmSourceInt: Provider<Int> = jvmSource.map { it.substringAfterLast('.').toInt() }

  final override val jvmToolchain: Property<String> = objects.property(String::class.java)
    .convention(latticeProperties.java.jvmToolchain)
  override val jvmToolchainInt: Provider<Int> = jvmToolchain.map {
    it.substringAfterLast('.').toInt()
  }
}
