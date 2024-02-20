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

package com.rickbusarow.mahout.conventions

import com.rickbusarow.kgx.gradleLazy
import com.rickbusarow.kgx.newInstanceLazy
import com.rickbusarow.mahout.config.MahoutProperties
import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.core.HasObjectFactory
import com.rickbusarow.mahout.core.SubExtension
import com.rickbusarow.mahout.core.SubExtensionInternal
import dev.drewhamilton.poko.Poko
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import javax.inject.Inject
import kotlin.reflect.KClass

/** */
@Poko
public open class SubExtensionRegistry @Inject constructor(
  @PublishedApi internal val objects: ObjectFactory
) {

  /** */
  public val schema: MutableMap<String, SubExtensionElement<*>> = mutableMapOf()

  internal inline fun <reified T : Any, reified R : T> delegate(
    rClass: KClass<out R> = R::class
  ): T {
    return objects.newInstance(rClass.java, this) as T
  }

  /** */
  public fun <T : SubExtension<*>> register(
    name: String,
    publicType: Class<T>,
    instanceType: Class<out T>
  ): T {
    val instance = objects.newInstance(instanceType)
    schema[name] = SubExtensionElement(publicType = publicType, name = name, instance = instance)
    return instance
  }

  /** */
  public inline fun <reified T : SubExtension<T>, reified R : T> register(
    name: String,
    instanceType: KClass<out R>
  ): T {
    val instance = objects.newInstance(instanceType.java)
    val publicType = T::class.java
    schema[name] = SubExtensionElement(publicType = publicType, name = name, instance = instance)
    return instance
  }

  /** */
  @Poko
  public class SubExtensionElement<out T : SubExtension<*>>(

    /** */
    public val publicType: Class<out T>,

    /** */
    public val name: String,

    /** */
    public val instance: T
  )
}

/** */
public abstract class AbstractHasSubExtension : HasObjectFactory {

  // protected inline fun <reified T : SubExtension<T>> subExtension(): Lazy<T> {
  //   return objects.newInstanceLazy<T>()
  // }

  protected fun <T : SubExtension<T>, R : T> subExtension(clazz: KClass<out R>): Lazy<R> {
    return objects.newInstanceLazy(clazz.java)
  }

  @Deprecated(
    "Use subExtension(clazz: KClass<out T>) instead.",
    ReplaceWith("subExtension(T::class)")
  )
  protected fun <T : SubExtension<T>, R : T> subExtension(clazz: Class<out R>): Lazy<R> {
    return objects.newInstanceLazy(clazz)
  }
}

/** */
public abstract class AbstractSubExtension @Inject constructor(
  protected val target: Project,
  final override val objects: ObjectFactory
) : SubExtensionInternal, ExtensionAware, ObjectFactory by objects {

  protected val mahoutProperties: MahoutProperties by gradleLazy { target.mahoutProperties }

  protected inline fun <reified T : SubExtension<T>> subExtension(): Lazy<T> {
    return objects.newInstanceLazy<T>()
  }

  protected fun <T : SubExtension<T>> subExtension(clazz: Class<out T>): Lazy<T> {
    return objects.newInstanceLazy(clazz)
  }
}
