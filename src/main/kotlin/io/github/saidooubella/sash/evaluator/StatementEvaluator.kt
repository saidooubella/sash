package io.github.saidooubella.sash.evaluator

import io.github.saidooubella.sash.compiler.refiner.nodes.*
import io.github.saidooubella.sash.compiler.utils.fastForEach
import io.github.saidooubella.sash.compiler.utils.fastZipToMutableMap
import io.github.saidooubella.sash.evaluator.control.ControlException
import io.github.saidooubella.sash.evaluator.utils.checkInstance
import io.github.saidooubella.sash.evaluator.values.BooleanValue
import io.github.saidooubella.sash.evaluator.values.NativeFunction
import io.github.saidooubella.sash.evaluator.values.RecordValue
import io.github.saidooubella.sash.evaluator.values.UnitValue

internal fun evalStatement(env: Environment, statement: Statement) {
    when (statement) {
        is ErrorStatement -> error("An error expression sneaked to the evaluator")
        is ExpressionStatement -> evalExpressionStatement(env, statement)
        is DefinitionStatement -> evalDefinitionStatement(env, statement)
        is RecordStatement -> evalRecordStatement(env, statement)
        is ReturnStatement -> evalReturnStatement(env, statement)
        is WhileStatement -> evalWhileStatement(env, statement)
        is YieldStatement -> evalYieldStatement(env, statement)
        is ContinueStatement -> evalContinueStatement()
        is BreakStatement -> evalBreakStatement()
        is EmptyStatement -> Unit
    }
}

private fun evalRecordStatement(env: Environment, statement: RecordStatement) {
    when (val fields = statement.record.fields) {
        null -> env.create(statement.definition.name, RecordValue(statement.record, mutableMapOf()))
        else -> env.create(statement.definition.name, NativeFunction(statement.definition.type) { args ->
            RecordValue(statement.record, fields.fastZipToMutableMap(args) { it.identifier })
        })
    }
}

private fun evalYieldStatement(env: Environment, statement: YieldStatement) {
    throw ControlException.Yield(evalExpression(env, statement.value))
}

private fun evalWhileStatement(env: Environment, statement: WhileStatement) {
    while (checkInstance<BooleanValue>(evalExpression(env, statement.condition)).value) {
        try {
            statement.body.fastForEach { evalStatement(env, it) }
        } catch (_: ControlException.Continue) {
            continue
        } catch (_: ControlException.Break) {
            break
        }
    }
}

private fun evalDefinitionStatement(env: Environment, statement: DefinitionStatement) {
    env.create(statement.definition.name, evalExpression(env, statement.expression))
}

private fun evalExpressionStatement(env: Environment, statement: ExpressionStatement) {
    @Suppress("CheckResult") evalExpression(env, statement.expression)
}

private fun evalReturnStatement(env: Environment, statement: ReturnStatement) {
    val value = statement.value?.let { evalExpression(env, it) }
    throw ControlException.Return(value ?: UnitValue)
}

private fun evalContinueStatement(): Unit = throw ControlException.Continue

private fun evalBreakStatement(): Unit = throw ControlException.Break
