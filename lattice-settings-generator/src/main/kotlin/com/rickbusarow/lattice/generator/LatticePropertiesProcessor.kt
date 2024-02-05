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

package com.rickbusarow.lattice.generator

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.rickbusarow.lattice.generator.utils.addAnnotation
import com.rickbusarow.lattice.generator.utils.applyEach
import com.rickbusarow.lattice.generator.utils.hasSuperType
import com.rickbusarow.lattice.generator.utils.maybeAddKdoc
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.Dynamic
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.withIndent

/** */
class LatticePropertiesProcessor(
  environment: SymbolProcessorEnvironment
) : LatticeSymbolProcessor(environment) {

  override fun process(resolver: Resolver): List<KSAnnotated> {

    val schemaFqName = requireNotNull(LatticePropertiesSchema::class.qualifiedName)

    for (symbol in resolver.getSymbolsWithAnnotation(schemaFqName)) {
      val latticeProperties = parseClass(symbol as KSClassDeclaration, emptyList())

      val implClassName = symbol.toClassName().impl()

      val fileSpec = FileSpec.builder(implClassName)
        .addGeneratedBy()
        .addType(latticeProperties)
        .addAnnotation(Suppress::class, "AbsentOrWrongFileLicense")
        .build()

      codeGenerator.createNewFile(
        dependencies = Dependencies(aggregating = false, requireNotNull(symbol.containingFile)),
        packageName = implClassName.packageName,
        fileName = implClassName.simpleName
      ).bufferedWriter().use { writer ->

        fileSpec.toString()
          .replace("`internal`", "internal")
          .also { writer.write(it) }
      }
    }

    return emptyList()
  }

  private fun parseClass(clazz: KSClassDeclaration, parentNames: List<String>): TypeSpec {

    clazz.check(clazz.hasSuperType(names.javaSerializable)) {
      "${clazz.toClassName()} must implement java.io.Serializable"
    }

    val nestedClasses = clazz.declarations
      .filterIsInstance<KSClassDeclaration>()

    val groupsByFqn = nestedClasses.associateBy { requireNotNull(it.qualifiedName).asString() }

    val groupTypes = groupsByFqn.keys

    val (groups, values) = clazz.getAllProperties()
      .partition { it.type.toTypeName().toString() in groupTypes }

    val clazzCN = clazz.toClassName()
    val implCN = clazzCN.impl()

    val builder = when {
      parentNames.isEmpty() -> createTopLevelBuilder(
        implClassName = implCN,
        defaultsClassName = clazzCN.defaults(),
        docString = clazz.docString,
        interfaceClassName = clazzCN
      )

      else -> createNestedBuilder(
        implClassName = implCN,
        docString = clazz.docString,
        interfaceClassName = clazzCN
      )
    }

    return builder
      .applyEach(values) { value ->
        addValueProperty(value, parentNames)
      }
      .applyEach(groups) { group ->

        val groupCN = group.type.resolve().toClassName()
        val groupCNString = groupCN.toString()
        val groupClass = groupsByFqn.getValue(groupCNString)

        val groupImplCN = groupCN.impl()

        addGroupProperty(
          groupPropertyName = group.simpleName.asString(),
          groupImplCN = groupImplCN,
          docString = group.docString
        )

        addType(parseClass(groupClass, parentNames + group.simpleName.asString()))
      }
      .addToString(groups, values, parentNames)
      .build()
  }

  private fun TypeSpec.Builder.addToString(
    groups: List<KSPropertyDeclaration>,
    values: List<KSPropertyDeclaration>,
    parentNames: List<String>
  ) = apply {
    addFunction(
      FunSpec.builder("toString")
        .addModifiers(KModifier.OVERRIDE)
        .returns(String::class)
        .addCode(
          buildCodeBlock {
            beginControlFlow("return buildString")

            for (v in values) {
              val simpleName = v.simpleName.asString()
              val qualifiedPropertyName = parentNames.joinToString(".", postfix = ".$simpleName")
              addStatement("appendLine(%P)", "$qualifiedPropertyName=\${$simpleName.orNull}")
            }

            if (values.isNotEmpty() && groups.isNotEmpty()) {
              addStatement("appendLine()")
            }

            for ((index, g) in groups.withIndex()) {
              addStatement("appendLine(%L)", g.simpleName.asString())
              if (index < groups.lastIndex) {
                addStatement("appendLine()")
              }
            }

            endControlFlow()
          }
        )
        .build()
    )
  }

  @OptIn(KspExperimental::class)
  private fun TypeSpec.Builder.addValueProperty(
    value: KSPropertyDeclaration,
    parentNames: List<String>
  ) = apply {
    val simpleName = value.simpleName.asString()
    val valueType = value.type.toTypeName() as ParameterizedTypeName

    val docString = value.docString?.trimIndent()

    addProperty(
      PropertySpec.builder(simpleName, valueType, KModifier.OVERRIDE)
        .maybeAddKdoc(docString)
        .initializer(
          buildCodeBlock {

            val propertyType = valueType.typeArguments.single()

            // ex: `lattice.kotlin.allWarningsAsErrors`
            val qualifiedPropertyName = listOf(
              "lattice",
              *parentNames.toTypedArray(),
              simpleName
            )
              .joinToString(separator = ".")

            add("providers\n.gradleProperty(%S)", qualifiedPropertyName)

            val delegateName = value.getAnnotationsByType(DelegateProperty::class)
              .flatMap { it.names.asSequence() }

            for (dn in delegateName) {
              add("\n.orElse(providers.gradleProperty(%S))", dn)
            }

            propertyType.propertyMapperOrNull()?.let { mapper ->
              add(mapper)
            }

            // ex: `defaults.kotlin.allWarningsAsErrors`
            val defaultName = listOf(
              "defaults",
              *parentNames.toTypedArray(),
              simpleName
            )
              .joinToString(separator = ".")

            add("\n.orElse(%L)", defaultName)
          }
        )
        .build()
    )
  }

  private fun TypeName.propertyMapperOrNull(): CodeBlock? {
    return when (val typeName = this) {
      is ParameterizedTypeName -> when (typeName.rawType) {
        names.set, names.list -> {
          buildCodeBlock {
            add("\n.map { string ->\n")
            withIndent {
              add("string.split(',', ' ')")

              typeName.typeArguments.single()
                .propertyMapperOrNull()
                ?.let { elementMapper ->
                  add("\n")
                  withIndent {
                    add("%L", elementMapper)
                  }
                }
            }
            add("\n}")
          }
        }

        else -> error("unsupported type: ${typeName.rawType}")
      }

      is ClassName ->
        @Suppress("ElseCaseInsteadOfExhaustiveWhen")
        when (typeName) {
          names.string -> null
          names.boolean -> CodeBlock.of(".map { it.toBoolean() }")
          names.int -> CodeBlock.of(".map { it.toInt() }")
          else -> error("unsupported type: $this")
        }

      Dynamic -> null
      is LambdaTypeName -> null
      is TypeVariableName -> null
      is WildcardTypeName -> null
    }
  }

  private fun TypeSpec.Builder.addGroupProperty(
    groupPropertyName: String,
    groupImplCN: ClassName,
    docString: String?
  ) = addProperty(
    PropertySpec.builder(groupPropertyName, groupImplCN, KModifier.OVERRIDE)
      .maybeAddKdoc(docString)
      .initializer("%T()", groupImplCN)
      .build()
  )

  private fun createTopLevelBuilder(
    implClassName: ClassName,
    defaultsClassName: ClassName,
    docString: String?,
    interfaceClassName: ClassName
  ): TypeSpec.Builder {

    val target = "target"
    val providers = "providers"
    return TypeSpec.classBuilder(implClassName)
      .addModifiers(KModifier.OPEN, KModifier.INTERNAL)
      .maybeAddKdoc(docString)
      .addSuperinterface(interfaceClassName)
      .primaryConstructor(
        FunSpec.constructorBuilder()
          .addAnnotation(names.javaxInject)
          .addParameter(providers, names.gradleProviderFactory)
          .addParameter(target, names.gradleProject)
          .build()
      )
      .addProperty(
        PropertySpec.builder(providers, names.gradleProviderFactory)
          .initializer(providers)
          .addModifiers(KModifier.PRIVATE)
          .build()
      )
      .addProperty(
        PropertySpec.builder("defaults", defaultsClassName)
          .initializer("%T(target = %L)", defaultsClassName, target)
          .addModifiers(KModifier.PRIVATE)
          .build()
      )
  }

  private fun createNestedBuilder(
    implClassName: ClassName,
    docString: String?,
    interfaceClassName: ClassName
  ): TypeSpec.Builder {
    return TypeSpec.classBuilder(implClassName)
      .addModifiers(KModifier.INNER, KModifier.INTERNAL)
      .maybeAddKdoc(docString)
      .addSuperinterface(interfaceClassName)
  }

  private fun ClassName.impl() = ClassName(
    "$packageName.internal",
    simpleNames.map { "${it}Impl" }
  )

  private fun ClassName.defaults() = ClassName(
    "$packageName.internal",
    simpleNames.map { "${it}Defaults" }
  )
}
