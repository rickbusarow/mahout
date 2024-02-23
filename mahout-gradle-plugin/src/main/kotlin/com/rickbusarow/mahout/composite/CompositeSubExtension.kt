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

package com.rickbusarow.mahout.composite

import com.rickbusarow.mahout.api.SubExtension
import com.rickbusarow.mahout.conventions.AbstractHasSubExtension
import dev.drewhamilton.poko.Poko
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.reflect.TypeOf
import org.gradle.api.specs.AndSpec
import org.gradle.api.specs.Spec
import javax.inject.Inject

/** */
public interface HasCompositeSubExtension : java.io.Serializable {

  /** */
  public val composite: CompositeSubExtension

  /** */
  public fun composite(action: Action<in CompositeSubExtension>) {
    action.execute(composite)
  }
}

internal abstract class DefaultHasCompositeSubExtension @Inject constructor(
  override val objects: ObjectFactory
) : AbstractHasSubExtension(), HasCompositeSubExtension {

  override val composite: CompositeSubExtension by subExtension(CompositeSubExtension::class)
}

/** */
public abstract class CompositeSubExtension @Inject constructor() : SubExtension<CompositeSubExtension> {

  internal var includeRequested: AndSpec<RequestedTask> = AndSpec()
  internal var includeCompositeTasks: AndSpec<ResolvedTask> = AndSpec()

  /** */
  public fun includeRequested(spec: Spec<RequestedTask>) {
    includeRequested = includeRequested.and(spec)
  }

  /** */
  public fun includeRequestedNames(vararg names: String) {
    includeRequested = includeRequested.and { names.contains(it.name) }
  }

  /** */
  public fun excludeRequestedNames(vararg names: String) {
    includeRequested = includeRequested.and { !names.contains(it.name) }
  }

  /** */
  public fun includeCompositeTasks(spec: Spec<ResolvedTask>) {
    includeCompositeTasks = includeCompositeTasks.and(spec)
  }

  /**
   * @property name the name of the task
   * @property typeOrNull the type of the task, or null to match any type
   */
  @Poko
  public class RequestedTask(
    public val name: String,
    public val typeOrNull: TypeOf<*>?
  ) {

    /** */
    public operator fun component1(): String = name

    /** */
    public operator fun component2(): TypeOf<*>? = typeOrNull
  }

  /**
   * @property buildPath
   * @property taskPath
   * @property taskName the name of the task
   * @property type the type of the task
   */
  @Poko
  public class ResolvedTask(
    public val buildPath: String,
    public val taskPath: String,
    public val taskName: String,
    public val type: TypeOf<*>
  ) {

    /** */
    public operator fun component1(): String = buildPath

    /** */
    public operator fun component2(): String = taskPath

    /** */
    public operator fun component3(): String = taskName

    /** */
    public operator fun component4(): TypeOf<*> = type
  }
}
