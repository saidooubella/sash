package io.github.saidooubella.sash.compiler.refiner

import io.github.saidooubella.sash.compiler.refiner.symbols.*

internal fun FunctionType.substitute(args: Map<TypeParam, Type>): FunctionType {
    return FunctionType(typeParams, valueParams.map { it.substitute(args) }, returnType.substitute(args))
}

private fun RecordType.substitute(args: Map<TypeParam, Type>): RecordType {
    val concreteTypes = buildMap { typeParams.forEach { param -> args[param]?.let { put(param, it) } } }
    return RecordType(typeParams, name, concreteTypes, fields?.map { RecordType.Field(it.identifier, it.type.substitute(args)) })
}

private fun Type.substitute(args: Map<TypeParam, Type>): Type {
    return when (this) {
        is TypeParam, is ErrorType -> this
        is PlaceholderType -> args[param] ?: this
        is FunctionType -> substitute(args)
        is RecordType -> substitute(args)
    }
}

///

internal fun FunctionType.withPlaceholders(): FunctionType {
    return if (typeParams.isNotEmpty()) withPlaceholders(typeParams.toSet()) else this
}

private fun Type.withPlaceholders(params: Set<TypeParam>): Type {
    return when (this) {
        is PlaceholderType -> error("Placeholder only occur on call sites.")
        is FunctionType -> withPlaceholders(params)
        is RecordType -> withPlaceholders(params)
        is TypeParam -> withPlaceholders(params)
        is ErrorType -> this
    }
}

private fun FunctionType.withPlaceholders(params: Set<TypeParam>): FunctionType {
    return FunctionType(typeParams, valueParams.map { it.withPlaceholders(params) }, returnType.withPlaceholders(params))
}

private fun RecordType.withPlaceholders(params: Set<TypeParam>): RecordType {
    return RecordType(typeParams, name, emptyMap(), fields?.map { RecordType.Field(it.identifier, it.type.withPlaceholders(params)) })
}

private fun TypeParam.withPlaceholders(params: Set<TypeParam>): Type {
    return if (this in params) PlaceholderType(this) else this
}

///

internal fun Type.hasPlaceholders(): Boolean {
    return when (this) {
        is FunctionType -> valueParams.any { it.hasPlaceholders() } || returnType.hasPlaceholders()
        is RecordType -> fields?.any { it.type.hasPlaceholders() } == true
        is TypeParam, is ErrorType -> false
        is PlaceholderType -> true
    }
}

///

internal fun Type.unwrap(): Type = if (this is PlaceholderType) param else this
