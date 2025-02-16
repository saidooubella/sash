package io.github.saidooubella.sash.evaluator

import io.github.saidooubella.sash.compiler.refiner.nodes.*
import io.github.saidooubella.sash.compiler.utils.fastForEach
import io.github.saidooubella.sash.compiler.utils.fastMap
import io.github.saidooubella.sash.evaluator.control.ControlException
import io.github.saidooubella.sash.evaluator.utils.catching
import io.github.saidooubella.sash.evaluator.utils.checkInstance
import io.github.saidooubella.sash.evaluator.utils.instanceOf
import io.github.saidooubella.sash.evaluator.values.*

internal fun evalExpression(env: Environment, expression: Expression): AnyValue {
    return when (expression) {
        is ErrorExpression -> error("An error expression sneaked to the evaluator")
        is LogicalBinaryExpression -> evalLogicalBinaryExpression(env, expression)
        is ParenthesizedExpression -> evalParenthesizedExpression(env, expression)
        is AssignmentExpression -> evalAssignmentExpression(env, expression)
        is IdentifierExpression -> evalIdentifierExpression(env, expression)
        is FunctionExpression -> evalFunctionExpression(env, expression)
        is BinaryExpression -> evalBinaryExpression(env, expression)
        is InvokeExpression -> evalInvokeExpression(env, expression)
        is UnaryExpression -> evalUnaryExpression(env, expression)
        is BooleanExpression -> evalBooleanExpression(expression)
        is DecimalExpression -> evalDecimalExpression(expression)
        is IntegerExpression -> evalIntegerExpression(expression)
        is StringExpression -> evalStringExpression(expression)
        is IfExpression -> evalIfExpression(env, expression)
    }
}

private fun evalIfExpression(env: Environment, expression: IfExpression): AnyValue {

    fun evalCondition(condition: Expression): Boolean {
        return checkInstance<BooleanValue>(evalExpression(env, condition)).value
    }

    return catching<ControlException.Yield, _>({ it.value }) {
        if (evalCondition(expression.condition)) {
            expression.ifBody.fastForEach { evalStatement(env, it) }
        } else {
            val body = expression.elseIfClauses.firstOrNull { evalCondition(it.condition) }?.body ?: expression.elseBody
            body?.fastForEach { evalStatement(env, it) }
        }
        UnitValue
    }
}

private fun evalParenthesizedExpression(env: Environment, expression: ParenthesizedExpression): AnyValue {
    return evalExpression(env, expression.expression)
}

private fun evalInvokeExpression(env: Environment, expression: InvokeExpression): AnyValue {
    return catching<ControlException.Return, _>({ it.value }) {
        val args = expression.args.fastMap { evalExpression(env, it) }
        checkInstance<AnyCallable>(evalExpression(env, expression.target)).call(args)
    }
}

private fun evalUnaryExpression(env: Environment, expression: UnaryExpression): AnyValue {

    val operand = evalExpression(env, expression.operand)

    return when (expression.operator) {
        UnaryOperator.LogicalNegation -> checkInstance<BooleanValue>(operand).not()
        UnaryOperator.Negation -> when (operand) {
            is IntegerValue -> operand.neg()
            is DecimalValue -> operand.neg()
            else -> error("Invalid operation: -${operand.type.stringify()}")
        }

        UnaryOperator.Identity -> when (operand) {
            is IntegerValue -> operand
            is DecimalValue -> operand
            else -> error("Invalid operation: +${operand.type.stringify()}")
        }
    }
}

private fun evalBinaryExpression(env: Environment, expression: BinaryExpression): AnyValue {

    val left = evalExpression(env, expression.left)
    val right = evalExpression(env, expression.right)

    return when (expression.operator) {
        BinaryOperator.Addition -> when {
            instanceOf<IntegerValue>(left, right) -> left add right
            instanceOf<DecimalValue>(left, right) -> left add right
            instanceOf<StringValue>(left, right) -> left concat right
            else -> error("Invalid operation: ${left.type.stringify()} + ${right.type.stringify()}")
        }

        BinaryOperator.Subtraction -> when {
            instanceOf<IntegerValue>(left, right) -> left sub right
            instanceOf<DecimalValue>(left, right) -> left sub right
            else -> error("Invalid operation: ${left.type.stringify()} - ${right.type.stringify()}")
        }

        BinaryOperator.Multiplication -> when {
            instanceOf<IntegerValue>(left, right) -> left mul right
            instanceOf<DecimalValue>(left, right) -> left mul right
            else -> error("Invalid operation: ${left.type.stringify()} * ${right.type.stringify()}")
        }

        BinaryOperator.Modulo -> when {
            instanceOf<IntegerValue>(left, right) -> left mod right
            instanceOf<DecimalValue>(left, right) -> left mod right
            else -> error("Invalid operation: ${left.type.stringify()} * ${right.type.stringify()}")
        }

        BinaryOperator.Division -> when {
            instanceOf<IntegerValue>(left, right) -> left div right
            instanceOf<DecimalValue>(left, right) -> left div right
            else -> error("Invalid operation: ${left.type.stringify()} / ${right.type.stringify()}")
        }

        BinaryOperator.GreaterThan -> when {
            instanceOf<IntegerValue>(left, right) -> left gt right
            instanceOf<DecimalValue>(left, right) -> left gt right
            else -> error("Invalid operation: ${left.type.stringify()} > ${right.type.stringify()}")
        }

        BinaryOperator.LessThan -> when {
            instanceOf<IntegerValue>(left, right) -> left lt right
            instanceOf<DecimalValue>(left, right) -> left lt right
            else -> error("Invalid operation: ${left.type.stringify()} < ${right.type.stringify()}")
        }

        BinaryOperator.GreaterThanOrEqual -> when {
            instanceOf<IntegerValue>(left, right) -> left gteq right
            instanceOf<DecimalValue>(left, right) -> left gteq right
            else -> error("Invalid operation: ${left.type.stringify()} >= ${right.type.stringify()}")
        }

        BinaryOperator.LessThanOrEqual -> when {
            instanceOf<IntegerValue>(left, right) -> left lteq right
            instanceOf<DecimalValue>(left, right) -> left lteq right
            else -> error("Invalid operation: ${left.type.stringify()} <= ${right.type.stringify()}")
        }

        BinaryOperator.Equal -> left.eq(right)
        BinaryOperator.NotEqual -> left.eq(right).not()
    }
}

private fun evalLogicalBinaryExpression(env: Environment, expression: LogicalBinaryExpression): AnyValue {
    val left = checkInstance<BooleanValue>(evalExpression(env, expression.left))
    return when (expression.operator) {
        LogicalBinaryOperator.Conjunction -> if (!left.value) left else checkInstance<BooleanValue>(evalExpression(env, expression.right))
        LogicalBinaryOperator.Disjunction -> if (left.value) left else checkInstance<BooleanValue>(evalExpression(env, expression.right))
    }
}

private fun evalAssignmentExpression(env: Environment, expression: AssignmentExpression): AnyValue {
    val value = evalExpression(env, expression.value)
    env.update(expression.target.symbol.name, value)
    return value
}

private fun evalFunctionExpression(env: Environment, expression: FunctionExpression): AnyValue {
    return SimpleFunction(env, expression.body, expression.params, expression.type)
}

private fun evalIdentifierExpression(env: Environment, expression: IdentifierExpression): AnyValue {
    return env.get(expression.symbol.name)
}

private fun evalStringExpression(expression: StringExpression): AnyValue {
    return StringValue(expression.value)
}

private fun evalBooleanExpression(expression: BooleanExpression): AnyValue {
    return BooleanValue(expression.value)
}

private fun evalIntegerExpression(expression: IntegerExpression): AnyValue {
    return IntegerValue(expression.value)
}

private fun evalDecimalExpression(expression: DecimalExpression): AnyValue {
    return DecimalValue(expression.value)
}
