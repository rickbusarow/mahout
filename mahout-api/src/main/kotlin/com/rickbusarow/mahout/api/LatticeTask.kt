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

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.VerificationTask
import org.gradle.api.tasks.bundling.Jar

/**
 * The most-common Mahout-specific task.
 *
 * @see DefaultMahoutTask for a version that Gradle can instantiate
 */
public interface MahoutTask : Task

/**
 * The most-common Mahout-specific task that Gradle can instantiate.
 *
 * @see MahoutTask for the interface version
 */
public abstract class DefaultMahoutTask : DefaultTask(), MahoutTask

/**
 * Marker interface for Mahout tasks which check something.
 *
 * @see DefaultMahoutCheckTask for a version that Gradle can instantiate
 */
public interface MahoutCheckTask : MahoutTask, VerificationTask

/**
 * A Mahout task that checks something and can be instantiated by Gradle.
 *
 * @see MahoutCheckTask for the interface version
 */
public abstract class DefaultMahoutCheckTask : DefaultMahoutTask(), MahoutCheckTask

/**
 * Marker interface for Mahout tasks which fix something.
 *
 * @see DefaultMahoutFixTask for a version that Gradle can instantiate
 */
public interface MahoutFixTask : MahoutTask, VerificationTask

/**
 * A Mahout task that fixes something and can be instantiated by Gradle.
 *
 * @see MahoutFixTask for the interface version
 */
public abstract class DefaultMahoutFixTask : DefaultMahoutTask(), MahoutFixTask

/**
 * Marker interface for Mahout tasks which generate code.
 *
 * @see DefaultMahoutCodeGeneratorTask for a version that Gradle can instantiate
 */
public interface MahoutCodeGeneratorTask : MahoutTask

/**
 * A Mahout task that generates code and can be instantiated by Gradle.
 *
 * @see MahoutCodeGeneratorTask for the interface version
 */
public abstract class DefaultMahoutCodeGeneratorTask :
  DefaultMahoutTask(),
  MahoutCodeGeneratorTask

/**
 * Marker interface for Mahout tasks which create a .jar
 *
 * @see DefaultMahoutJarTask for a version that Gradle can instantiate
 */
public interface MahoutJarTask : MahoutTask

/**
 * A Mahout task that creates a .jar and can be instantiated by Gradle.
 *
 * @see MahoutJarTask for the interface version
 */
public abstract class DefaultMahoutJarTask : Jar(), MahoutJarTask

/**
 * Marker interface for Mahout tasks which create a Javadoc .jar
 *
 * @see DefaultMahoutJavadocJarTask for a version that Gradle can instantiate
 */
public interface MahoutJavadocJarTask : MahoutJarTask

/**
 * A Mahout task that creates a Javadoc .jar and can be instantiated by Gradle.
 *
 * @see MahoutJavadocJarTask for the interface version
 */
public abstract class DefaultMahoutJavadocJarTask : Jar(), MahoutJavadocJarTask

/**
 * Marker interface for Mahout tasks which create a source-code .jar
 *
 * @see DefaultMahoutSourcesJarTask for a version that Gradle can instantiate
 */
public interface MahoutSourcesJarTask : MahoutJarTask

/**
 * A Mahout task that creates a source-code .jar and can be instantiated by Gradle.
 *
 * @see MahoutSourcesJarTask for the interface version
 */
public abstract class DefaultMahoutSourcesJarTask : Jar(), MahoutSourcesJarTask
