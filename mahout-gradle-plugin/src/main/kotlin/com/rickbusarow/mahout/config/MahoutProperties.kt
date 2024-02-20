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

package com.rickbusarow.mahout.config

import com.rickbusarow.kgx.extras
import com.rickbusarow.kgx.getOrPut
import com.rickbusarow.mahout.config.MahoutProperties.JavaSettingsGroup
import com.rickbusarow.mahout.config.MahoutProperties.RepositorySettingsGroup.GithubSettingsGroup
import com.rickbusarow.mahout.generator.DelegateProperty
import com.rickbusarow.mahout.generator.MahoutPropertiesSchema
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.io.Serializable as JavaSerializable

/** */
public val Project.mahoutProperties: MahoutProperties
  get() = extras.getOrPut("mahoutProperties") {
    objects.newInstance(
      com.rickbusarow.mahout.config.internal.MahoutPropertiesImpl::class.java
    )
  }

/** */
@MahoutPropertiesSchema
public interface MahoutProperties : JavaSerializable {

  /** */
  @DelegateProperty("GROUP")
  public val group: Provider<String>

  /** */
  @DelegateProperty("VERSION_NAME")
  public val versionName: Provider<String>

  /** */
  public val versions: VersionsGroup

  /** */
  public interface VersionsGroup : JavaSerializable {

    /**
     * ```properties
     * mahout.versions.detekt=1.9.20
     * ```
     */
    public val detekt: Provider<String>

    /**
     * The Dokka version to be used by the Dokkatoo plugin.
     *
     * ```properties
     * mahout.versions.dokka=1.9.20
     * ```
     */
    public val dokka: Provider<String>

    /**
     * ```properties
     * mahout.versions.kotlinxSerialization=1.2.3
     * ```
     */
    public val kotlinxSerialization: Provider<String>

    /**
     * ```properties
     * mahout.versions.ksp=1.2.3
     * ```
     */
    public val ksp: Provider<String>

    /**
     * ```properties
     * mahout.versions.poko=1.2.3
     * ```
     */
    public val poko: Provider<String>
  }

  /** */
  public val kotlin: KotlinSettingsGroup

  /** */
  public interface KotlinSettingsGroup : JavaSerializable {

    /** Any additional Kotlin compiler arguments, such as `-Xjvm-default=all`. */
    public val compilerArgs: Provider<List<String>>

    /** */
    public val apiLevel: Provider<String>

    /** If true, Kotlin's `warningsAsErrors` functionality is enabled full-time. */
    public val allWarningsAsErrors: Provider<Boolean>

    /** If true, adds the `-Xexplicit-api=strict` compiler argument. */
    public val explicitApi: Provider<Boolean>
  }

  /**
   * ```properties
   * mahout.java.___
   * ```
   */
  public val java: JavaSettingsGroup

  /** */
  public interface JavaSettingsGroup : JavaSerializable {

    /**
     * ```properties
     * mahout.java.jvmSource=1.8 # or 11, etc.
     * ```
     */
    public val jvmSource: Provider<String>

    /**
     * ```properties
     * mahout.java.jvmTarget=1.8 # or 11, etc.
     * ```
     */
    public val jvmTarget: Provider<String>

    /**
     * ```properties
     * mahout.java.jvmToolchain=1.8 # or 11, etc.
     * ```
     */
    public val jvmToolchain: Provider<String>
  }

  /** */
  public val repository: RepositorySettingsGroup

  /** */
  public interface RepositorySettingsGroup : JavaSerializable {

    /** */
    public val defaultBranch: Provider<String>

    /** */
    public val github: GithubSettingsGroup

    /** */
    public interface GithubSettingsGroup : JavaSerializable {

      /** */
      public val owner: Provider<String>

      /** */
      public val repo: Provider<String>
    }
  }

  /** */
  public val publishing: PublishingSettingsGroup

  /** */
  public interface PublishingSettingsGroup : JavaSerializable {

    /** */
    public val pom: PomSettingsGroup

    /** */
    public interface PomSettingsGroup : JavaSerializable {

      /** */
      @DelegateProperty("POM_ARTIFACT_ID")
      public val artifactId: Provider<String>

      /** */
      @DelegateProperty("POM_NAME")
      public val name: Provider<String>

      /** */
      @DelegateProperty("POM_DESCRIPTION")
      public val description: Provider<String>

      /** */
      @DelegateProperty("POM_INCEPTION_YEAR")
      public val inceptionYear: Provider<String>

      /** */
      @DelegateProperty("POM_URL")
      public val url: Provider<String>

      /** */
      public val license: LicenseSettingsGroup

      /** */
      public interface LicenseSettingsGroup : JavaSerializable {
        /** */
        @DelegateProperty("POM_LICENSE_NAME")
        public val name: Provider<String>

        /** */
        @DelegateProperty("POM_LICENSE_URL")
        public val url: Provider<String>

        /** */
        @DelegateProperty("POM_LICENSE_DIST")
        public val dist: Provider<String>
      }

      /** */
      public val scm: ScmSettingsGroup

      /** */
      public interface ScmSettingsGroup : JavaSerializable {

        /** */
        @DelegateProperty("POM_SCM_URL")
        public val url: Provider<String>

        /** */
        @DelegateProperty("POM_SCM_CONNECTION")
        public val connection: Provider<String>

        /** */
        @DelegateProperty("POM_SCM_DEV_CONNECTION")
        public val devConnection: Provider<String>
      }

      /** */
      public val developer: DeveloperSettingsGroup

      /** */
      public interface DeveloperSettingsGroup : JavaSerializable {

        /** */
        @DelegateProperty("POM_DEVELOPER_ID")
        public val id: Provider<String>

        /** */
        @DelegateProperty("POM_DEVELOPER_NAME")
        public val name: Provider<String>

        /** */
        @DelegateProperty("POM_DEVELOPER_URL")
        public val url: Provider<String>
      }
    }
  }
}

/** */
public val GithubSettingsGroup.url: Provider<String>
  get() = owner.zip(repo) { owner, repo -> "https://github.com/$owner/$repo" }

/** */
public val JavaSettingsGroup.jvmTargetInt: Provider<Int>
  get() = jvmTarget.map { it.substringAfterLast('.').toInt() }

/** */
public val JavaSettingsGroup.jvmSourceInt: Provider<Int>
  get() = jvmSource.map { it.substringAfterLast('.').toInt() }

/** */
public val JavaSettingsGroup.jvmToolchainInt: Provider<Int>
  get() = jvmToolchain.map { it.substringAfterLast('.').toInt() }
