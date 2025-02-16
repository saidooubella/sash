package io.github.saidooubella.sash.compiler.refiner.nodes

import io.github.saidooubella.sash.compiler.refiner.symbols.Definition
import io.github.saidooubella.sash.compiler.refiner.symbols.RecordType
import io.github.saidooubella.sash.compiler.span.Position
import io.github.saidooubella.sash.compiler.span.Spanned

public sealed interface Statement : Spanned

public data class ErrorStatement internal constructor(
    override val start: Position,
    override val end: Position,
) : Statement {
    internal constructor(spanned: Spanned) : this(spanned.start, spanned.end)
}

public data class YieldStatement internal constructor(
    val value: Expression,
    override val start: Position,
    override val end: Position,
) : Statement

public data class EmptyStatement internal constructor(
    override val start: Position,
    override val end: Position,
) : Statement

public data class ExpressionStatement internal constructor(
    val expression: Expression,
    override val start: Position,
    override val end: Position,
) : Statement

public data class DefinitionStatement internal constructor(
    val definition: Definition,
    val expression: Expression,
    override val start: Position,
    override val end: Position,
) : Statement

public data class RecordStatement internal constructor(
    val definition: Definition,
    val record: RecordType,
    override val start: Position,
    override val end: Position,
) : Statement

public data class ReturnStatement internal constructor(
    val value: Expression?,
    override val start: Position,
    override val end: Position,
) : Statement

public data class WhileStatement internal constructor(
    val condition: Expression,
    val body: List<Statement>,
    override val start: Position,
    override val end: Position,
) : Statement

public data class ContinueStatement internal constructor(
    override val start: Position,
    override val end: Position,
) : Statement

public data class BreakStatement internal constructor(
    override val start: Position,
    override val end: Position,
) : Statement
