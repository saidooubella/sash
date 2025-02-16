package io.github.saidooubella.sash.compiler.refiner

import io.github.saidooubella.sash.compiler.refiner.context.RefinerContext
import io.github.saidooubella.sash.compiler.refiner.nodes.*
import io.github.saidooubella.sash.compiler.refiner.symbols.ErrorType
import io.github.saidooubella.sash.compiler.refiner.symbols.NothingType
import io.github.saidooubella.sash.compiler.refiner.symbols.Type
import io.github.saidooubella.sash.compiler.refiner.symbols.UnitType
import io.github.saidooubella.sash.compiler.utils.fastForEach

internal fun checkReturnPaths(context: RefinerContext, statements: List<Statement>, returnType: Type): Boolean {
    return checkReturnPaths(context, statements) || returnType.assignableTo(UnitType) || returnType == ErrorType
}

private fun checkReturnPaths(context: RefinerContext, statements: List<Statement>): Boolean {
    var result = false
    statements.fastForEach {
        if (result) {
            context.reporter.reportUnreachableCode(it.start, it.end)
        } else {
            result = checkReturnPaths(context, it)
        }
    }
    return result
}

private fun checkReturnPaths(context: RefinerContext, statement: Statement): Boolean {
    return when (statement) {
        is WhileStatement, is EmptyStatement, is ErrorStatement, is YieldStatement, is RecordStatement -> false
        is ReturnStatement, is BreakStatement, is ContinueStatement -> true
        is DefinitionStatement -> checkReturnPaths(context, statement.expression)
        is ExpressionStatement -> checkReturnPaths(context, statement.expression)
    }
}

private fun checkReturnPaths(context: RefinerContext, expression: Expression): Boolean {
    return when (expression) {
        is BooleanExpression, is DecimalExpression, is IntegerExpression, is StringExpression,
        is ErrorExpression, is FunctionExpression, is IdentifierExpression -> false
        is LogicalBinaryExpression -> checkReturnPaths(context, expression.left) or checkReturnPaths(context, expression.right)
        is BinaryExpression -> checkReturnPaths(context, expression.left) or checkReturnPaths(context, expression.right)
        is ParenthesizedExpression -> checkReturnPaths(context, expression.expression)
        is AssignmentExpression -> checkReturnPaths(context, expression.value)
        is UnaryExpression -> checkReturnPaths(context, expression.operand)
        is IfExpression -> checkReturnPaths(context, expression)
        is InvokeExpression -> {
            expression.args.fastForEach { argument ->
                if (checkReturnPaths(context, argument)) {
                    context.reporter.reportUnreachableCode(expression.start, argument.start)
                    context.reporter.reportUnreachableCode(argument.end, expression.end)
                    return true
                }
            }
            expression.type.assignableTo(NothingType)
        }
    }
}

private fun checkReturnPaths(context: RefinerContext, expression: IfExpression): Boolean {
    val ifBlock = checkReturnPaths(context, expression.ifBody)
    val elseIfBlocks = expression.elseIfClauses.all { checkReturnPaths(context, it.body) }
    val elseBlock = expression.elseBody != null && checkReturnPaths(context, expression.elseBody)
    return ifBlock && elseIfBlocks && elseBlock
}
