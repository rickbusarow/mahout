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

package com.rickbusarow.mahout.publishing

import com.rickbusarow.kgx.get
import com.rickbusarow.kgx.gradleLazy
import com.rickbusarow.kgx.javaExtension
import com.rickbusarow.kgx.named
import com.rickbusarow.kgx.names.SourceSetName.Companion.addSuffix
import com.rickbusarow.kgx.names.SourceSetName.Companion.asSourceSetName
import com.rickbusarow.kgx.names.SourceSetName.Companion.isMain
import com.rickbusarow.kgx.names.TaskName.Companion.asTaskName
import com.rickbusarow.kgx.newInstance
import com.rickbusarow.kgx.register
import com.rickbusarow.mahout.api.SubExtension
import com.rickbusarow.mahout.api.SubExtensionInternal
import com.rickbusarow.mahout.config.mahoutProperties
import com.rickbusarow.mahout.conventions.AbstractHasSubExtension
import com.rickbusarow.mahout.conventions.AbstractSubExtension
import com.rickbusarow.mahout.core.stdlib.letIf
import com.rickbusarow.mahout.core.versionIsSnapshot
import com.rickbusarow.mahout.dokka.DokkatooConventionPlugin.Companion.dokkaJavadocJar
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPom
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugins.signing.Sign
import javax.inject.Inject

/** */
public interface HasPublishingMavenSubExtension : java.io.Serializable {

  /** */
  public val publishing: PublishingMavenSubExtension

  /** */
  public fun publishing(action: Action<in PublishingMavenSubExtension>) {
    action.execute(publishing)
  }
}

internal abstract class DefaultHasPublishingMavenSubExtension @Inject constructor(
  override val objects: ObjectFactory
) : AbstractHasSubExtension(),
  HasPublishingMavenSubExtension,
  SubExtensionInternal {

  override val publishing: PublishingMavenSubExtension by
    subExtension(DefaultPublishingMavenSubExtension::class)
}

/** */
public interface PublishingMavenSubExtension : SubExtension<PublishingMavenSubExtension> {

  /** */
  public val defaultPom: DefaultMavenPom

  /** */
  public fun publishMaven(
    artifactId: String? = null,
    pomDescription: String? = null,
    groupId: String? = null,
    versionName: String? = null,
    sourceSetName: String = "main",
    publicationName: String? = null,
    configureAction: Action<in MavenPublication>? = null
  )
}

/** */
public abstract class DefaultPublishingMavenSubExtension @Inject constructor(
  target: Project,
  objects: ObjectFactory
) : AbstractSubExtension(target, objects),
  PublishingMavenSubExtension,
  SubExtensionInternal {

  override val defaultPom: DefaultMavenPom by gradleLazy {
    objects.newInstance<DefaultMavenPom>()
      .also { pom ->
        pom.url.convention(mahoutProperties.publishing.pom.url)
        pom.name.convention(mahoutProperties.publishing.pom.name)

        pom.description.convention(mahoutProperties.publishing.pom.description)
        pom.inceptionYear.convention(mahoutProperties.publishing.pom.inceptionYear)

        pom.licenses { licenseSpec ->
          licenseSpec.license { license ->

            license.name.convention(mahoutProperties.publishing.pom.license.name)
            license.url.convention(mahoutProperties.publishing.pom.license.url)
            license.distribution.convention(mahoutProperties.publishing.pom.license.dist)
          }
        }

        pom.scm { scm ->

          scm.url.convention(mahoutProperties.publishing.pom.scm.url)
          scm.connection.convention(mahoutProperties.publishing.pom.scm.connection)
          scm.developerConnection.convention(mahoutProperties.publishing.pom.scm.devConnection)
        }

        pom.developers { developerSpec ->
          developerSpec.developer { developer ->

            developer.id.convention(mahoutProperties.publishing.pom.developer.id)
            developer.name.convention(mahoutProperties.publishing.pom.developer.name)
            developer.url.convention(mahoutProperties.publishing.pom.developer.url)
          }
        }
      }
  }

  override fun publishMaven(
    artifactId: String?,
    pomDescription: String?,
    groupId: String?,
    versionName: String?,
    sourceSetName: String,
    publicationName: String?,
    configureAction: Action<in MavenPublication>?
  ) {

    val ssName = sourceSetName.asSourceSetName()

    val pubName = publicationName
      ?: PublicationName.forSourceSetName(baseName = "maven", sourceSetName = ssName).value

    val sourcesJarTaskName = "sourcesJar".asTaskName()
      .letIf(!ssName.isMain()) { ssName.addSuffix(it) }

    target.tasks.register(sourcesJarTaskName, Jar::class) {
      it.archiveClassifier.set("sources")
      it.from(target.javaExtension.sourceSets[ssName].allSource)
    }

    target.gradlePublishingExtension.publications
      .register(pubName, MavenPublication::class.java) { publication ->

        publication.artifactId = artifactId
          ?: mahoutProperties.publishing.pom.artifactId.orNull
          ?: target.name
        publication.groupId = groupId
          ?: target.mahoutProperties.group.orNull
          ?: target.group.toString()
        publication.version = versionName
          ?: target.mahoutProperties.versionName.orNull
          ?: target.version.toString()

        val sourcesJar = target.tasks.named(sourcesJarTaskName)

        publication.from(target.components.getByName("java"))

        publication.artifact(sourcesJar)
        publication.artifact(target.tasks.dokkaJavadocJar)

        publication.pom.description.set(pomDescription)

        val default = defaultPom

        publication.pom { mavenPom ->

          mavenPom.url.convention(default.url)
          mavenPom.name.convention(default.name)
          mavenPom.description.convention(default.description)
          mavenPom.inceptionYear.convention(default.inceptionYear)

          mavenPom.licenses { licenseSpec ->

            for (defaultLicense in default.licenses) {
              licenseSpec.license { license ->
                license.name.convention(defaultLicense.name)
                license.url.convention(defaultLicense.url)
                license.distribution.convention(defaultLicense.distribution)
              }
            }
          }

          mavenPom.scm { scm ->
            default.scm?.url?.let { scm.url.convention(it) }
            default.scm?.connection?.let { scm.connection.convention(it) }
            default.scm?.developerConnection?.let { scm.developerConnection.convention(it) }
          }

          mavenPom.developers { developerSpec ->
            for (defaultDeveloper in default.developers) {
              developerSpec.developer { developer ->
                developer.id.convention(defaultDeveloper.id)
                developer.name.convention(defaultDeveloper.name)
                developer.url.convention(defaultDeveloper.url)
              }
            }
          }
        }
      }

    target.tasks.withType(Sign::class.java).configureEach {

      // skip signing for -SNAPSHOT publishing
      it.onlyIf { !target.versionIsSnapshot }
    }
  }
}
