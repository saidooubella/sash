package io.github.saidooubella.sash.compiler.refiner

import io.github.saidooubella.sash.compiler.parser.nodes.RawControlBody
import io.github.saidooubella.sash.compiler.parser.nodes.RawExpression
import io.github.saidooubella.sash.compiler.refiner.context.RefinerContext
import io.github.saidooubella.sash.compiler.refiner.nodes.Expression
import io.github.saidooubella.sash.compiler.refiner.nodes.Statement
import io.github.saidooubella.sash.compiler.refiner.symbols.BooleanType
import io.github.saidooubella.sash.compiler.utils.fastMap

internal fun refineControlBody(context: RefinerContext, block: RawControlBody): List<Statement> {
    return block.statements.fastMap { refineStatement(context, it) }
}

internal fun refineControlFlowCondition(context: RefinerContext, condition: RawExpression): Expression {

    @Suppress("NAME_SHADOWING")
    val condition = context.withContextualType(BooleanType) {
        refineExpression(context, condition, ExpressionMode.NotCallable)
    }

    if (!condition.type.assignableTo(BooleanType)) {
        context.reporter.reportInvalidConditionType(condition.start, condition.end)
    }

    return condition
}
