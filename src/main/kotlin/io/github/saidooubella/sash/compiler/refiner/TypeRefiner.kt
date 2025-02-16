package io.github.saidooubella.sash.compiler.refiner

import io.github.saidooubella.sash.compiler.parser.nodes.RawFunctionType
import io.github.saidooubella.sash.compiler.parser.nodes.RawSimpleType
import io.github.saidooubella.sash.compiler.parser.nodes.RawType
import io.github.saidooubella.sash.compiler.refiner.context.RefinerContext
import io.github.saidooubella.sash.compiler.refiner.symbols.ErrorType
import io.github.saidooubella.sash.compiler.refiner.symbols.FunctionType
import io.github.saidooubella.sash.compiler.refiner.symbols.Type
import io.github.saidooubella.sash.compiler.tokens.TokenType
import io.github.saidooubella.sash.compiler.utils.map

internal fun refineType(context: RefinerContext, type: RawType): Type {
    return when (type) {
        is RawFunctionType -> refineFunctionType(context, type)
        is RawSimpleType -> refineSimpleType(context, type)
    }
}

private fun refineFunctionType(context: RefinerContext, type: RawFunctionType): Type {
    val valueParams = type.valueParams.map { refineType(context, it) }
    return FunctionType(listOf(), valueParams, refineType(context, type.returnType))
}

private fun refineSimpleType(context: RefinerContext, type: RawSimpleType): Type {
    if (type.name.type == TokenType.Injected) return ErrorType
    type.typeArgs?.args?.map { refineType(context, it) }
    return context.getType(type.name.text) ?: run {
        context.reporter.reportUndefinedSymbol(type.start, type.end, type.name.text)
        ErrorType
    }
}
