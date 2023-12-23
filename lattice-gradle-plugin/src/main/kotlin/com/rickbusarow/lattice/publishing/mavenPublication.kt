/*
 * Copyright (C) 2023 Rick Busarow
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

package com.rickbusarow.lattice.publishing

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension

internal fun MavenPublication.isPluginMarker(): Boolean = name.endsWith("PluginMarkerMaven")
internal fun MavenPublication.nameWithoutMarker(): String = name.removeSuffix("PluginMarkerMaven")
internal fun Publication.isPluginMarker(): Boolean =
  (this as? MavenPublication)?.isPluginMarker() ?: false

internal val Project.mavenPublishBaseExtension: MavenPublishBaseExtension
  get() = extensions.getByType(MavenPublishBaseExtension::class.java)

internal val Project.gradlePublishingExtension: PublishingExtension
  get() = extensions.getByType(PublishingExtension::class.java)

internal val Project.gradlePluginExtension: GradlePluginDevelopmentExtension
  get() = extensions.getByType(GradlePluginDevelopmentExtension::class.java)

internal val Project.mavenPublications: NamedDomainObjectSet<MavenPublication>
  get() = gradlePublishingExtension.publications.withType(MavenPublication::class.java)
