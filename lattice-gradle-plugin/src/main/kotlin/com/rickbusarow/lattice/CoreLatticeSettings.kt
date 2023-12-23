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

package com.rickbusarow.lattice

import com.rickbusarow.lattice.config.latticeProperties
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

public interface CoreLatticeProperties {
  public val group: Property<String>
  public val versionName: Property<String>
}

public open class DefaultCoreLatticeProperties @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : CoreLatticeProperties {

  override val group: Property<String> = objects.property(String::class.java)
    .convention(target.latticeProperties.group)

  override val versionName: Property<String> = objects.property(String::class.java)
    .convention(target.latticeProperties.versionName)
}
