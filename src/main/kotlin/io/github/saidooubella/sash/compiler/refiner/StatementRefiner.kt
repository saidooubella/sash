package io.github.saidooubella.sash.compiler.refiner

import io.github.saidooubella.sash.compiler.parser.nodes.*
import io.github.saidooubella.sash.compiler.refiner.context.*
import io.github.saidooubella.sash.compiler.refiner.nodes.*
import io.github.saidooubella.sash.compiler.refiner.symbols.*
import io.github.saidooubella.sash.compiler.tokens.TokenType
import io.github.saidooubella.sash.compiler.utils.mapNotNull

internal fun refineStatement(context: RefinerContext, statement: RawStatement): Statement {
    return when (statement) {
        is ImplicitResultRawStatement -> refineImplicitResultStatement(context, statement)
        is DefinitionRawStatement -> refineDefinitionStatement(context, statement)
        is ExpressionRawStatement -> refineExpressionStatement(context, statement)
        is ContinueRawStatement -> refineContinueStatement(context, statement)
        is ReturnRawStatement -> refineReturnStatement(context, statement)
        is BreakRawStatement -> refineBreakStatement(context, statement)
        is WhileRawStatement -> refineWhileStatement(context, statement)
        is DropStatement -> refineDropStatement(context, statement)
        is EmptyRawStatement -> refineEmptyStatement(statement)
    }
}

private fun refineDropStatement(context: RefinerContext, statement: DropStatement): Statement {

    val expression = refineExpression(context, statement.expression, ExpressionMode.NotCallable)

    if (expression.type != ErrorType && expression.type.assignableTo(UnitType)) {
        context.reporter.reportUnitDiscard(expression.start, expression.end)
    }

    return ExpressionStatement(expression, statement.start, statement.end)
}

private fun refineEmptyStatement(statement: EmptyRawStatement): Statement {
    return EmptyStatement(statement.start, statement.end)
}

private fun refineExpressionStatement(context: RefinerContext, statement: ExpressionRawStatement): Statement {

    val expression = context.withContextualType(null) {
        refineExpression(context, statement.expression, ExpressionMode.Statement + ExpressionMode.NotCallable)
    }

    if (expression !is AssignmentExpression && !expression.type.assignableTo(UnitType)) {
        context.reporter.reportUnusedExpression(expression.start, expression.end)
    }

    return ExpressionStatement(expression, statement.start, statement.end)
}

private fun refineContinueStatement(context: RefinerContext, statement: ContinueRawStatement): Statement {
    context.currentScope<LoopScope>() ?: context.reporter.reportInvalidContinueUsage(statement.start, statement.end)
    return ContinueStatement(statement.start, statement.end)
}

private fun refineBreakStatement(context: RefinerContext, statement: BreakRawStatement): Statement {
    context.currentScope<LoopScope>()
        ?: context.reporter.reportInvalidBreakUsage(statement.start, statement.end)
    return BreakStatement(statement.start, statement.end)
}

private fun refineWhileStatement(context: RefinerContext, statement: WhileRawStatement): Statement {
    val condition = refineControlFlowCondition(context, statement.condition)
    val body = context.scoped(LoopScope) { refineControlBody(context, statement.body) }
    return WhileStatement(condition, body, statement.start, statement.end)
}

private fun refineReturnStatement(context: RefinerContext, statement: ReturnRawStatement): Statement {

    val functionScope = context.currentScope<FunctionScope>()

    val value = context.withContextualType(functionScope?.returnType) {
        statement.expression?.let { refineExpression(context, it, ExpressionMode.NotCallable) }
    }

    val ret = statement.returnKeyword

    if (functionScope == null) {
        context.reporter.reportInvalidReturnUsage(ret.start, ret.end)
    } else {

        val returnType = functionScope.returnType

        if (returnType == null) {
            functionScope.returnType = value?.type ?: UnitType
        } else if (value == null && !returnType.assignableTo(UnitType)) {
            context.reporter.reportMissingReturnValue(ret.start, ret.end, returnType)
        } else if (value?.type?.assignableTo(returnType) == false) {
            context.reporter.reportInvalidReturnType(ret.start, ret.end, value.type, returnType)
        } else {
            functionScope.returnType = value?.type ?: UnitType
        }
    }

    return ReturnStatement(value, statement.start, statement.end)
}

private fun refineImplicitResultStatement(context: RefinerContext, statement: ImplicitResultRawStatement): Statement {
    return when (val scope = context.currentScope<ContextualScope>()) {
        is FunctionScope -> implicitReturnStatement(context, statement, scope)
        is LoopScope -> implicitExpressionStatement(context, statement)
        is IfScope -> implicitYieldStatement(context, statement, scope)
        null -> {
            refineExpression(context, statement.expression, ExpressionMode.NotCallable)
            context.reporter.reportInvalidImplicitResultUsage(statement.start, statement.end)
            ErrorStatement(statement)
        }
    }
}

private fun implicitExpressionStatement(context: RefinerContext, statement: ImplicitResultRawStatement): ExpressionStatement {

    val value = context.withContextualType(null) {
        refineExpression(context, statement.expression, ExpressionMode.NotCallable)
    }

    if (!value.type.assignableTo(UnitType)) {
        context.reporter.reportInvalidYieldType(value.start, value.end, value.type, UnitType)
    }

    return ExpressionStatement(value, statement.start, statement.end)
}

private fun implicitYieldStatement(context: RefinerContext, statement: ImplicitResultRawStatement, scope: IfScope): YieldStatement {

    val yieldedType = scope.yieldedType

    val value = context.withContextualType(yieldedType) {
        refineExpression(context, statement.expression, ExpressionMode.NotCallable)
    }

    if (yieldedType == null) {
        scope.yieldedType = value.type
    } else if (!value.type.assignableTo(yieldedType)) {
        context.reporter.reportInvalidYieldType(value.start, value.end, value.type, yieldedType)
    } else {
        scope.yieldedType = value.type
    }

    return YieldStatement(value, statement.start, statement.end)
}

private fun implicitReturnStatement(context: RefinerContext, statement: ImplicitResultRawStatement, scope: FunctionScope): ReturnStatement {

    val returnType = scope.returnType

    val value = context.withContextualType(returnType) {
        refineExpression(context, statement.expression, ExpressionMode.NotCallable)
    }

    if (returnType == null) {
        scope.returnType = value.type
    } else if (!value.type.assignableTo(returnType)) {
        context.reporter.reportInvalidReturnType(value.start, value.end, value.type, returnType)
    } else {
        scope.returnType = value.type
    }

    return ReturnStatement(value, statement.start, statement.end)
}

private fun refineDefinitionStatement(context: RefinerContext, statement: DefinitionRawStatement): Statement {
    return when (statement.initializer) {
        is ExpressionRawInitializer -> simpleDefinitionStatement(context, statement, statement.initializer.expression)
        is RecordRawInitializer -> recordDefinitionStatement(context, statement, statement.initializer.fields)
        is EnumRawInitializer -> TODO()
    }
}

private fun recordDefinitionStatement(context: RefinerContext, statement: DefinitionRawStatement, rawFields: RawFields?): Statement {

    val identifier = statement.identifier
    val typeParams = refineTypeParams(statement, context)

    val recordType = RecordType(typeParams, identifier.text)

    if (statement.mutKeyword != null) {
        context.reporter.reportMutableRecordDefinition(statement.mutKeyword.start, statement.mutKeyword.end)
    }

    val hasDefinition = context.hasDefinition(identifier.text)

    if (identifier.type != TokenType.Injected) {
        if (hasDefinition) {
            context.reporter.reportDuplicateSymbol(identifier.start, identifier.end, identifier.text)
        } else {
            context.putType(recordType)
        }
    }

    val annotatedType: Type?

    context.scoped {

        typeParams.forEach(context::putType)

        annotatedType = statement.typeAnnotation?.type?.let { withTypeParams(refineType(context, it), typeParams) }

        val seenNames = hashSetOf<String>()

        recordType.fields = rawFields?.fields?.mapNotNull {
            val name = it.identifier
            if (name.type == TokenType.Injected) return@mapNotNull null
            if (!seenNames.add(name.text)) context.reporter.reportDuplicateSymbol(name.start, name.end, name.text)
            RecordType.Field(name.text, refineType(context, it.type))
        }
    }

    if (recordType.fields == null && statement.typeParams != null) {
        context.reporter.reportParametrizedUnitRecord(statement.typeParams.start, statement.typeParams.end)
    }

    val actualType = recordType.fields?.let { FunctionType(typeParams, it.map(RecordType.Field::type), recordType) } ?: recordType
    val type = annotatedType ?: actualType

    val definition = Definition(recordType.name, type, statement.mutKeyword == null)
    if (!hasDefinition) context.putDefinition(definition)

    validateTypes(context, statement.typeAnnotation, type, actualType)
    return RecordStatement(definition, recordType, statement.start, statement.end)
}

private fun simpleDefinitionStatement(context: RefinerContext, statement: DefinitionRawStatement, initializer: RawExpression): Statement {

    val typeParams = refineTypeParams(statement, context)

    val annotatedType: Type?
    val value: Expression

    context.scoped {
        typeParams.forEach(context::putType)
        annotatedType = statement.typeAnnotation?.type?.let { withTypeParams(refineType(context, it), typeParams) }
        value = context.withContextualType(annotatedType) { refineExpression(context, initializer, ExpressionMode.NotCallable) }
    }

    if (statement.typeParams != null && statement.mutKeyword != null) {
        context.reporter.reportMutableParametrizedDefinition(statement.mutKeyword.start, statement.mutKeyword.end)
    }

    if (statement.typeParams != null && value.type !is FunctionType) {
        context.reporter.reportNonFunctionParametrizedDefinition(statement.initializer.start, statement.initializer.end)
    }

    val type = annotatedType ?: withTypeParams(value.type, typeParams)

    val name = statement.identifier
    val definition = Definition(name.text, type, statement.mutKeyword == null)

    if (name.type != TokenType.Injected) {
        if (context.hasDefinition(name.text)) {
            context.reporter.reportDuplicateSymbol(name.start, name.end, name.text)
        } else {
            context.putDefinition(definition)
        }
    }

    validateTypes(context, statement.typeAnnotation, definition.type, value.type)
    return DefinitionStatement(definition, value, statement.start, statement.end)
}

private fun validateTypes(context: RefinerContext, typeAnnotation: RawTypeAnnotation?, expected: Type, actual: Type) {
    if (typeAnnotation != null && !actual.assignableTo(expected)) {
        context.reporter.reportAssignmentTypeMismatch(typeAnnotation.type.start, typeAnnotation.type.end, actual, expected)
    }
}

private fun refineTypeParams(statement: DefinitionRawStatement, context: RefinerContext): List<TypeParam> {
    val seenNames = hashSetOf<String>()
    return statement.typeParams?.typeParams?.mapNotNull {
        when {
            it.type == TokenType.Injected -> null
            it.text in seenNames -> {
                context.reporter.reportDuplicateSymbol(it.start, it.end, it.text)
                null
            }
            else -> TypeParam(it.text)
        }
    }.orEmpty()
}

private fun withTypeParams(type: Type, typeParams: List<TypeParam>): Type {
    return if (type is FunctionType && typeParams.isNotEmpty()) FunctionType(typeParams, type.valueParams, type.returnType) else type
}
