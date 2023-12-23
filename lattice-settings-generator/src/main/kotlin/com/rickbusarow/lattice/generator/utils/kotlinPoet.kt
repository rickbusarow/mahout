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

package com.rickbusarow.lattice.generator.utils

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.Annotatable
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.Documentable
import com.squareup.kotlinpoet.ksp.toTypeName
import kotlin.reflect.KClass

internal fun <T : Annotatable.Builder<*>> T.addAnnotation(
  clazz: KClass<out Annotation>,
  vararg stringArgs: String
): T = addAnnotation(clazz) {
  for (arg in stringArgs) {
    addMember("%S", arg)
  }
}

internal inline fun <T : Annotatable.Builder<*>> T.addAnnotation(
  clazz: KClass<out Annotation>,
  builder: AnnotationSpec.Builder.() -> Unit
): T = apply {
  addAnnotation(AnnotationSpec.builder(clazz).apply(builder).build())
}

internal fun <T : Documentable.Builder<*>> T.maybeAddKdoc(
  declaration: KSDeclaration
): T = maybeAddKdoc(declaration.docString)

internal fun <T : Documentable.Builder<*>> T.maybeAddKdoc(
  docString: String?
): T = apply {
  if (docString.isNullOrBlank()) return@apply
  addKdoc("%L", docString.trimIndent())
}

internal fun KSClassDeclaration.hasSuperType(type: ClassName): Boolean {
  return getAllSuperTypes().any { it.toTypeName().toString() == type.canonicalName }
}
