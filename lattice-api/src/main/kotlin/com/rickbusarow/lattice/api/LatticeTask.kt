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

package com.rickbusarow.lattice.api

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Jar

/**
 * The most-common Lattice-specific task.
 *
 * @see DefaultLatticeTask for a version that Gradle can instantiate
 */
public interface LatticeTask : Task

/**
 * The most-common Lattice-specific task that Gradle can instantiate.
 *
 * @see LatticeTask for the interface version
 */
public abstract class DefaultLatticeTask : DefaultTask(), LatticeTask

/**
 * Marker interface for Lattice tasks which check something.
 *
 * @see DefaultLatticeCheckTask for a version that Gradle can instantiate
 */
public interface LatticeCheckTask : LatticeTask

/**
 * A Lattice task that checks something and can be instantiated by Gradle.
 *
 * @see LatticeCheckTask for the interface version
 */
public abstract class DefaultLatticeCheckTask : DefaultLatticeTask(), LatticeCheckTask

/**
 * Marker interface for Lattice tasks which fix something.
 *
 * @see DefaultLatticeFixTask for a version that Gradle can instantiate
 */
public interface LatticeFixTask : LatticeTask

/**
 * A Lattice task that fixes something and can be instantiated by Gradle.
 *
 * @see LatticeFixTask for the interface version
 */
public abstract class DefaultLatticeFixTask : DefaultLatticeTask(), LatticeFixTask

/**
 * Marker interface for Lattice tasks which generate code.
 *
 * @see DefaultLatticeCodeGeneratorTask for a version that Gradle can instantiate
 */
public interface LatticeCodeGeneratorTask : LatticeTask

/**
 * A Lattice task that generates code and can be instantiated by Gradle.
 *
 * @see LatticeCodeGeneratorTask for the interface version
 */
public abstract class DefaultLatticeCodeGeneratorTask :
  DefaultLatticeTask(),
  LatticeCodeGeneratorTask

/**
 * Marker interface for Lattice tasks which create a .jar
 *
 * @see DefaultLatticeJarTask for a version that Gradle can instantiate
 */
public interface LatticeJarTask : LatticeTask

/**
 * A Lattice task that creates a .jar and can be instantiated by Gradle.
 *
 * @see LatticeJarTask for the interface version
 */
public abstract class DefaultLatticeJarTask : Jar(), LatticeJarTask

/**
 * Marker interface for Lattice tasks which create a Javadoc .jar
 *
 * @see DefaultLatticeJavadocJarTask for a version that Gradle can instantiate
 */
public interface LatticeJavadocJarTask : LatticeJarTask

/**
 * A Lattice task that creates a Javadoc .jar and can be instantiated by Gradle.
 *
 * @see LatticeJavadocJarTask for the interface version
 */
public abstract class DefaultLatticeJavadocJarTask : Jar(), LatticeJavadocJarTask

/**
 * Marker interface for Lattice tasks which create a source-code .jar
 *
 * @see DefaultLatticeSourcesJarTask for a version that Gradle can instantiate
 */
public interface LatticeSourcesJarTask : LatticeJarTask

/**
 * A Lattice task that creates a source-code .jar and can be instantiated by Gradle.
 *
 * @see LatticeSourcesJarTask for the interface version
 */
public abstract class DefaultLatticeSourcesJarTask : Jar(), LatticeSourcesJarTask
