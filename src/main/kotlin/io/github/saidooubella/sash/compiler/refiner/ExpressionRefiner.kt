package io.github.saidooubella.sash.compiler.refiner

import io.github.saidooubella.sash.compiler.parser.nodes.*
import io.github.saidooubella.sash.compiler.refiner.context.FunctionScope
import io.github.saidooubella.sash.compiler.refiner.context.IfScope
import io.github.saidooubella.sash.compiler.refiner.context.RefinerContext
import io.github.saidooubella.sash.compiler.refiner.nodes.*
import io.github.saidooubella.sash.compiler.refiner.symbols.*
import io.github.saidooubella.sash.compiler.tokens.Token
import io.github.saidooubella.sash.compiler.tokens.TokenType
import io.github.saidooubella.sash.compiler.utils.*
import kotlin.math.min

@JvmInline
internal value class ExpressionMode private constructor(private val value: Int) {

    operator fun plus(mode: ExpressionMode) = ExpressionMode(this.value or mode.value)
    operator fun get(mode: ExpressionMode) = this.value and mode.value != 0

    companion object {
        val None = ExpressionMode(0)
        val Statement = ExpressionMode(1)
        val NotCallable = ExpressionMode(2)
    }
}

internal fun refineExpression(context: RefinerContext, expression: RawExpression, expressionMode: ExpressionMode): Expression {
    return when (expression) {
        is ParenthesizedRawExpression -> refineParenthesizedExpression(context, expression)
        is IdentifierRawExpression -> refineIdentifierExpression(context, expression, expressionMode)
        is LogicalBinaryRawExpression -> refineLogicalBinaryExpression(context, expression)
        is AssignmentRawExpression -> refineAssignmentExpression(context, expression)
        is FunctionRawExpression -> refineFunctionExpression(context, expression)
        is DecimalRawExpression -> refineDecimalExpression(context, expression)
        is IntegerRawExpression -> refineIntegerExpression(context, expression)
        is IfRawExpression -> refineIfExpression(context, expression, expressionMode)
        is BinaryRawExpression -> refineBinaryExpression(context, expression)
        is InvokeRawExpression -> refineInvokeExpression(context, expression)
        is UnaryRawExpression -> refineUnaryExpression(context, expression)
        is BooleanRawExpression -> refineBooleanExpression(expression)
        is StringRawExpression -> refineStringExpression(expression)
        is AccessRawExpression -> refineAccessExpression(context, expression)
    }
}

@Suppress("UNUSED_PARAMETER")
private fun refineAccessExpression(context: RefinerContext, expression: AccessRawExpression): Expression {
    TODO("Not yet implemented")
}

private fun refineIfExpression(context: RefinerContext, expression: IfRawExpression, expressionMode: ExpressionMode): IfExpression {

    if (!expressionMode[ExpressionMode.Statement] && expression.elseClause == null) {
        context.reporter.reportIfMissingElse(expression.ifKeyword.start, expression.ifKeyword.start)
    }

    val condition = refineControlFlowCondition(context, expression.condition)
    val isExpression = !expressionMode[ExpressionMode.Statement] && expression.elseClause != null

    val scope = IfScope(if (isExpression) context.currentContextualType<Type>() else UnitType)

    return context.scoped(scope) {
        val ifBody = validateBlockResult(context, scope, expression.body)
        val elseIfClauses = expression.elseIfClauses.map { refineElseIfClause(context, scope, it) }
        val elseBody = expression.elseClause?.body?.let { validateBlockResult(context, scope, it) }
        val type = scope.yieldedType ?: UnitType
        IfExpression(condition, ifBody, elseIfClauses, elseBody, type, expression.start, expression.end)
    }
}

private fun refineElseIfClause(context: RefinerContext, scope: IfScope, clause: ElseIfRawClause): ElseIfClause {
    val condition = refineControlFlowCondition(context, clause.condition)
    return ElseIfClause(condition, validateBlockResult(context, scope, clause.body))
}

private fun validateBlockResult(context: RefinerContext, scope: IfScope, block: RawControlBody): List<Statement> {

    val body = refineControlBody(context, block)

    if (scope.yieldedType?.assignableTo(UnitType) == false && body.lastOrNull()?.cast<YieldStatement>() == null) {
        when (val yieldedType = scope.yieldedType) {
            null -> scope.yieldedType = UnitType
            else -> context.reporter.reportMissingResultValue(block.closeBrace.start, block.closeBrace.end, yieldedType)
        }
    }

    return body
}

private fun refineParenthesizedExpression(context: RefinerContext, expression: ParenthesizedRawExpression): Expression {
    val inner = refineExpression(context, expression.expression, ExpressionMode.NotCallable)
    return ParenthesizedExpression(inner)
}

private fun refineAssignmentExpression(context: RefinerContext, expression: AssignmentRawExpression): Expression {

    val target = context.withContextualType(null) { refineExpression(context, expression.target, ExpressionMode.NotCallable) }
    val value = context.withContextualType(target.type) { refineExpression(context, expression.value, ExpressionMode.NotCallable) }

    if (target.type == ErrorType) return ErrorExpression(expression)

    return when (target) {
        is IdentifierExpression -> assignmentExpression(context, expression, target, value)
        else -> {
            context.reporter.reportInvalidAssignmentTarget(target.start, target.end)
            ErrorExpression(expression)
        }
    }
}

private fun assignmentExpression(
    context: RefinerContext,
    expression: AssignmentRawExpression,
    target: IdentifierExpression,
    value: Expression,
): AssignmentExpression {

    if (!value.type.assignableTo(target.type)) {
        context.reporter.reportAssignmentTypeMismatch(value.start, value.end, target.type, value.type)
        ErrorExpression(expression)
    }

    if (target.symbol.readonly) {
        context.reporter.reportReadonlyAssignment(expression.assign.start, expression.assign.end)
        ErrorExpression(expression)
    }

    return AssignmentExpression(target, value, expression.start, expression.end)
}

private fun refineInvokeExpression(context: RefinerContext, expression: InvokeRawExpression): Expression {

    val target = context.withContextualType(null) { refineExpression(context, expression.target, ExpressionMode.None) }
    val typeArgs = expression.typeArgs?.args?.map { refineType(context, it) }
    val targetType = target.type

    if (targetType == ErrorType) {
        processArguments(context, expression.valueArgs)
        return ErrorExpression(expression)
    }

    if (targetType !is FunctionType) {
        processArguments(context, expression.valueArgs)
        context.reporter.reportNotFunctionTarget(expression.target.start, expression.target.end)
        return ErrorExpression(expression)
    }

    val fnType = targetType.withPlaceholders()

    if (typeArgs != null && typeArgs.size != fnType.typeParams.size) {
        context.reporter.reportUnmatchedTypeArgsCount(
            expression.typeArgs.start,
            expression.typeArgs.end,
            fnType.typeParams.size,
            typeArgs.size
        )
    }

    val inferredTypes = if (typeArgs != null && fnType.typeParams.isNotEmpty()) {
        fnType.typeParams.fastZipToMutableMap(typeArgs) { it }
    } else {
        hashMapOf()
    }

    val (args, type) = validateArguments(context, inferredTypes, fnType, expression.valueArgs)

    if (typeArgs == null && fnType.typeParams.isNotEmpty()) {
        val unspecifiedTypes = fnType.typeParams.subtract(inferredTypes.keys)
        if (unspecifiedTypes.isNotEmpty()) {
            context.reporter.reportUnspecifiedTypes(expression.target.start, expression.target.end, unspecifiedTypes)
        }
    }

    return InvokeExpression(target, type, args, type.returnType, expression.start, expression.end)
}

private fun extractContextualTypes(
    destination: MutableMap<TypeParam, Type>,
    contextualReturn: Type?,
    type: FunctionType,
    args: List<Type>,
) {
    fun extractTypes(maybeParam: Type, maybeArg: Type) {
        val param = maybeParam.unwrap()
        val arg = maybeArg.unwrap()
        if (param is FunctionType && arg is FunctionType) {
            extractContextualTypes(destination, arg.returnType, param, arg.valueParams)
        } else if (param is TypeParam && param != arg) {
            if (param !in destination) destination[param] = arg
        }
    }

    type.valueParams.fastZipEach(args) { param, arg -> extractTypes(param, arg) }
    if (contextualReturn != null) extractTypes(type.returnType, contextualReturn)
}

private fun validateArguments(
    context: RefinerContext,
    inferredTypes: MutableMap<TypeParam, Type>,
    type: FunctionType,
    args: ValueRawArgs,
): Pair<List<Expression>, FunctionType> {

    val result = processArguments(context, inferredTypes, args, type)

    val arguments = result.first
    val params = result.second.valueParams

    if (params.size != arguments.size) {
        context.reporter.reportUnmatchedArgsCount(args.start, args.end, params.size, arguments.size)
    }

    repeat(min(arguments.size, params.size)) { index ->
        val argument = arguments[index]
        val expectedType = params[index]
        if (!argument.type.assignableTo(expectedType)) {
            context.reporter.reportArgumentTypeMismatch(argument.start, argument.end, argument.type, expectedType)
        }
    }

    return result
}

private fun processArguments(
    context: RefinerContext,
    inferredTypes: MutableMap<TypeParam, Type>,
    args: ValueRawArgs,
    type: FunctionType,
): Pair<List<Expression>, FunctionType> {

    var finalType = type.substitute(inferredTypes)

    val contextualReturn = context.currentContextualType<Type>()
    val refinedArgs = mutableListOf<Expression>()

    fun handleRefinedArg(expression: Expression) {
        refinedArgs += expression
        extractContextualTypes(inferredTypes, contextualReturn, finalType, refinedArgs.map { it.type })
        finalType = finalType.substitute(inferredTypes)
    }

    when (args) {
        is SimpleRawArgs -> {
            args.valueArgs.forEachIndexed { index, expression ->
                context.withContextualType(finalType.valueParams.getOrNull(index)) {
                    handleRefinedArg(refineExpression(context, expression, ExpressionMode.NotCallable))
                }
            }
        }

        is TailFunctionRawArgs -> {
            val values = args.args?.valueArgs.orEmpty()
            values.forEachIndexed { index, expression ->
                context.withContextualType(finalType.valueParams.getOrNull(index)) {
                    handleRefinedArg(refineExpression(context, expression, ExpressionMode.NotCallable))
                }
            }
            context.withContextualType(finalType.valueParams.getOrNull(values.elementsSize)) {
                handleRefinedArg(refineFunctionExpression(context, args.trailingFn))
            }
        }
    }

    if (refinedArgs.isEmpty()) {
        extractContextualTypes(inferredTypes, contextualReturn, finalType, emptyList())
        finalType = finalType.substitute(inferredTypes)
    }

    return refinedArgs to finalType
}

private fun processArguments(context: RefinerContext, args: ValueRawArgs) {
    context.withContextualType(null) {
        when (args) {
            is SimpleRawArgs -> {
                args.valueArgs.forEach { refineExpression(context, it, ExpressionMode.NotCallable) }
            }

            is TailFunctionRawArgs -> {
                val values = args.args?.valueArgs.orEmpty()
                values.forEach { refineExpression(context, it, ExpressionMode.NotCallable) }
                refineFunctionExpression(context, args.trailingFn)
            }
        }
    }
}

private fun refineFunctionExpression(context: RefinerContext, expression: FunctionRawExpression): Expression {

    val functionType = context.currentContextualType<FunctionType>()

    val params = refineFunctionParams(
        context, expression.openBrace, expression.params, functionType?.valueParams
    )

    val fnScope = FunctionScope(functionType?.returnType)

    val statements = context.scoped(fnScope) {
        params.fastForEach { param -> context.putDefinition(Definition(param.name, param.type, true)) }
        expression.statements.fastMap { refineStatement(context, it) }
    }

    val returnType = fnScope.returnType ?: UnitType

    if (!checkReturnPaths(context, statements, returnType)) {
        context.reporter.reportRequiredReturnValue(expression.closeBrace.start, expression.closeBrace.end)
    }

    val type = FunctionType(listOf(), params.map(Parameter::type), returnType)
    return FunctionExpression(params, statements, type, expression.start, expression.end)
}

private fun refineFunctionParams(
    context: RefinerContext,
    openBrace: Token,
    rawParams: FunctionRawSignature?,
    contextualParams: List<Type>?,
): List<Parameter> {
    val params = rawParams?.params.orEmpty()

    if (contextualParams != null && params.elementsSize != contextualParams.size) {
        context.reporter.reportUnmatchedArgsCount(
            openBrace.start,
            openBrace.end,
            contextualParams.size,
            params.elementsSize,
        )
    }

    val seenNames = hashSetOf<String>()
    val parameters = mutableListOf<Parameter>()

    params.forEachIndexed { index, parameter ->

        val name = parameter.identifier.takeUnless { it.type == TokenType.Injected } ?: return@forEachIndexed

        if (!seenNames.add(name.text)) {
            context.reporter.reportDuplicateSymbol(name.start, name.end, name.text)
        }

        val annotation = parameter.type?.type

        val contextualType = contextualParams?.getOrNull(index)
        val annotationType = annotation?.let { refineType(context, it) }

        if (annotationType != null && contextualType != null && !contextualType.assignableTo(annotationType)) {
            context.reporter.reportArgumentTypeMismatch(annotation.start, annotation.end, annotationType, contextualType)
        }

        val type = annotationType ?: contextualType

        if (type == null) {
            context.reporter.reportCannotInferType(name.start, name.end, name.text)
        }

        parameters += Parameter(name.text, type ?: ErrorType, parameter.start, parameter.end)
    }

    return parameters
}

private fun refineIdentifierExpression(context: RefinerContext, expression: IdentifierRawExpression, expressionMode: ExpressionMode): Expression {

    if (expression.identifier.type == TokenType.Injected) return ErrorExpression(expression)

    val symbol = context.getDefinition(expression.identifier.text) ?: run {
        context.reporter.reportUndefinedSymbol(expression.start, expression.end, expression.identifier.text)
        return ErrorExpression(expression)
    }

    val type = symbol.type

    return if (expressionMode[ExpressionMode.NotCallable] && type is FunctionType && type.typeParams.isNotEmpty()) {
        context.reporter.reportStandaloneGenericType(expression.start, expression.end, expression.identifier.text)
        ErrorExpression(expression)
    } else {
        IdentifierExpression(symbol, type, expression.start, expression.end)
    }
}

private fun refineUnaryExpression(context: RefinerContext, expression: UnaryRawExpression): Expression {

    val operand = context.withContextualType(null) { refineExpression(context, expression.operand, ExpressionMode.NotCallable) }

    if (operand.type == ErrorType) return ErrorExpression(expression)

    val operator = expression.operator
    val unaryOperator = refineUnaryOperator(operator)

    val type = resolveUnaryOperationType(unaryOperator, operand.type) ?: run {
        context.reporter.reportInvalidUnaryOperation(operator.start, operator.end, operator.text, operand.type)
        return ErrorExpression(operator)
    }

    return UnaryExpression(unaryOperator, operand, type, expression.start, expression.end)
}

private fun refineLogicalBinaryExpression(context: RefinerContext, expression: LogicalBinaryRawExpression): Expression {

    val left = context.withContextualType(null) { refineExpression(context, expression.left, ExpressionMode.NotCallable) }
    val right = context.withContextualType(null) { refineExpression(context, expression.right, ExpressionMode.NotCallable) }

    if (right.type == ErrorType || left.type == ErrorType) return ErrorExpression(expression)

    val operator = expression.operator
    val logicalOperator = refineLogicalBinaryOperator(operator)

    val type = resolveLogicalBinaryOperationType(left.type, right.type) ?: run {
        context.reporter.reportInvalidBinaryOperation(operator.start, operator.end, operator.text, left.type, right.type)
        return ErrorExpression(operator)
    }

    return LogicalBinaryExpression(left, logicalOperator, right, type, expression.start, expression.end)
}

private fun refineBinaryExpression(context: RefinerContext, expression: BinaryRawExpression): Expression {

    val left = context.withContextualType(null) { refineExpression(context, expression.left, ExpressionMode.NotCallable) }
    val right = context.withContextualType(null) { refineExpression(context, expression.right, ExpressionMode.NotCallable) }

    if (right.type == ErrorType || left.type == ErrorType) return ErrorExpression(expression)

    val operator = expression.operator
    val binaryOperator = refineBinaryOperator(operator)

    val type = resolveBinaryOperationType(left.type, binaryOperator, right.type) ?: run {
        context.reporter.reportInvalidBinaryOperation(operator.start, operator.end, operator.text, left.type, right.type)
        return ErrorExpression(operator)
    }

    return BinaryExpression(left, binaryOperator, right, type, expression.start, expression.end)
}

private fun refineDecimalExpression(context: RefinerContext, expression: DecimalRawExpression): Expression {
    val literal = expression.value.text.toFloatOrNull() ?: expression.run {
        context.reporter.reportNumberOverflow(start, end, value.text, DecimalType)
        return ErrorExpression(expression)
    }
    return DecimalExpression(literal, expression.start, expression.end)
}

private fun refineIntegerExpression(context: RefinerContext, expression: IntegerRawExpression): Expression {
    val literal = expression.value.text.toIntOrNull() ?: expression.run {
        context.reporter.reportNumberOverflow(start, end, value.text, IntegerType)
        return ErrorExpression(expression)
    }
    return IntegerExpression(literal, expression.start, expression.end)
}

private fun refineBooleanExpression(expression: BooleanRawExpression): Expression {
    val value = expression.value.type == TokenType.TrueKeyword
    return BooleanExpression(value, expression.start, expression.end)
}

private fun refineStringExpression(expression: StringRawExpression): Expression {
    val literal = expression.value?.text ?: ""
    return StringExpression(literal, expression.start, expression.end)
}

private inline fun <reified T> Any?.cast(): T? = this as? T
