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

package com.rickbusarow.mahout.core

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.language.base.plugins.LifecycleBasePlugin

internal val Project.javaToolchainService: JavaToolchainService
  get() = extensions.getByType(JavaToolchainService::class.java)

internal val TaskContainer.clean: TaskProvider<Task>
  get() = named(LifecycleBasePlugin.CLEAN_TASK_NAME)

internal val TaskContainer.check: TaskProvider<Task>
  get() = named(LifecycleBasePlugin.CHECK_TASK_NAME)
