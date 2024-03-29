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

package com.rickbusarow.mahout.api

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware

/** Convenience interface for making a configurable group of settings. */
@MahoutDsl
public interface SubExtension<SELF : SubExtension<SELF>> :
  ExtensionAware,
  java.io.Serializable {

  /** */
  public fun configure(action: Action<in SELF>) {
    @Suppress("UNCHECKED_CAST")
    action.execute(this as SELF)
  }
}

/** */
public interface HasObjectFactory {

  /** */
  public val objects: ObjectFactory
}

/** */
@MahoutDsl
public interface SubExtensionInternal : HasObjectFactory

public typealias GradleSourceSet = org.gradle.api.tasks.SourceSet
public typealias KotlinSourceSet = org.jetbrains.kotlin.gradle.model.SourceSet
