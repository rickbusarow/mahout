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

import com.rickbusarow.kgx.names.DomainObjectName
import com.rickbusarow.kgx.names.SourceSetName
import com.rickbusarow.kgx.names.SourceSetName.Companion.addPrefix
import com.rickbusarow.kgx.names.SourceSetName.Companion.isMain
import org.gradle.api.publish.Publication

@JvmInline
internal value class PublicationName(override val value: String) : DomainObjectName<Publication> {
  companion object {

    fun forSourceSetName(baseName: String, sourceSetName: String): PublicationName {
      return forSourceSetName(baseName, SourceSetName(sourceSetName))
    }

    fun forSourceSetName(baseName: String, sourceSetName: SourceSetName): PublicationName {
      return if (sourceSetName.isMain()) {
        PublicationName(baseName)
      } else {
        PublicationName(sourceSetName.addPrefix(baseName))
      }
    }

    fun String.asPublicationName(): PublicationName = PublicationName(this)
  }
}
