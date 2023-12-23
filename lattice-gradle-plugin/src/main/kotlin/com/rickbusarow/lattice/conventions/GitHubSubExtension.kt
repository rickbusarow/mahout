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

import com.rickbusarow.lattice.config.url
import com.rickbusarow.lattice.core.SubExtension
import com.rickbusarow.lattice.core.SubExtensionInternal
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.reflect.HasPublicType
import org.gradle.api.reflect.TypeOf
import org.gradle.api.reflect.TypeOf.typeOf
import javax.inject.Inject

public interface HasGitHubSubExtension : java.io.Serializable {
  public val github: GitHubSubExtension

  public fun github(action: Action<in GitHubSubExtension>) {
    action.execute(github)
  }
}

internal abstract class DefaultHasGitHubSubExtension @Inject constructor(
  final override val objects: ObjectFactory
) : AbstractHasSubExtension(), HasGitHubSubExtension {

  override val github: GitHubSubExtension by subExtension(DefaultGitHubSubExtension::class)
}

public interface GitHubSubExtension : SubExtension<GitHubSubExtension> {
  public val owner: Property<String>
  public val repo: Property<String>
  public val defaultBranch: Property<String>
  public val url: Provider<String>
  public val connection: Provider<String>
  public val developerConnection: Provider<String>
}

public abstract class DefaultGitHubSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : AbstractSubExtension(target, objects),
  GitHubSubExtension,
  SubExtensionInternal,
  HasPublicType {

  override fun getPublicType(): TypeOf<*> = typeOf(GitHubSubExtension::class.java)

  final override val owner: Property<String> = objects.property(String::class.java)
    .convention(latticeProperties.repository.github.owner)

  final override val repo: Property<String> = objects.property(String::class.java)
    .convention(latticeProperties.repository.github.repo)

  override val defaultBranch: Property<String> = objects.property(String::class.java)
    .convention(latticeProperties.repository.defaultBranch)

  override val url: Provider<String> = latticeProperties.repository.github.url
    .orElse(owner.zip(repo) { owner, repo -> "https://github.com/$owner/$repo" })
  override val connection: Provider<String> = latticeProperties.repository.github.url
    .orElse(owner.zip(repo) { owner, repo -> "scm:git:git://github.com/$owner/$repo.git" })
  override val developerConnection: Provider<String> = latticeProperties.repository.github.url
    .orElse(owner.zip(repo) { owner, repo -> "scm:git:ssh://github.com/$owner/$repo.git" })
}
