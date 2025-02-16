package io.github.saidooubella.sash.compiler.refiner.nodes

import io.github.saidooubella.sash.compiler.refiner.symbols.*
import io.github.saidooubella.sash.compiler.span.Position
import io.github.saidooubella.sash.compiler.span.Spanned

public sealed interface Expression : Spanned, Typed

public data class IfExpression internal constructor(
    val condition: Expression,
    val ifBody: List<Statement>,
    val elseIfClauses: List<ElseIfClause>,
    val elseBody: List<Statement>?,
    override val type: Type,
    override val start: Position,
    override val end: Position,
) : Expression

public data class ElseIfClause internal constructor(
    val condition: Expression,
    val body: List<Statement>,
)

public data class AssignmentExpression internal constructor(
    val target: IdentifierExpression,
    val value: Expression,
    override val start: Position,
    override val end: Position,
) : Expression {
    override val type: Type get() = target.type
}

public data class ParenthesizedExpression internal constructor(
    val expression: Expression,
) : Expression {
    override val type: Type get() = expression.type
    override val start: Position get() = expression.start
    override val end: Position get() = expression.end
}

public sealed interface BinaryOperator {
    public object Addition : BinaryOperator
    public object Subtraction : BinaryOperator
    public object Multiplication : BinaryOperator
    public object Division : BinaryOperator
    public object GreaterThan : BinaryOperator
    public object LessThan : BinaryOperator
    public object LessThanOrEqual : BinaryOperator
    public object GreaterThanOrEqual : BinaryOperator
    public object Equal : BinaryOperator
    public object NotEqual : BinaryOperator
    public object Modulo : BinaryOperator
}

public data class BinaryExpression internal constructor(
    val left: Expression,
    val operator: BinaryOperator,
    val right: Expression,
    override val type: Type,
    override val start: Position,
    override val end: Position,
) : Expression

public sealed interface LogicalBinaryOperator {
    public object Disjunction : LogicalBinaryOperator
    public object Conjunction : LogicalBinaryOperator
}

public data class LogicalBinaryExpression internal constructor(
    val left: Expression,
    val operator: LogicalBinaryOperator,
    val right: Expression,
    override val type: Type,
    override val start: Position,
    override val end: Position,
) : Expression

public sealed interface UnaryOperator {
    public object LogicalNegation : UnaryOperator
    public object Identity : UnaryOperator
    public object Negation : UnaryOperator
}

public data class UnaryExpression internal constructor(
    val operator: UnaryOperator,
    val operand: Expression,
    override val type: Type,
    override val start: Position,
    override val end: Position,
) : Expression

public data class DecimalExpression internal constructor(
    val value: Float,
    override val start: Position,
    override val end: Position,
) : Expression {
    override val type: Type get() = DecimalType
}

public data class ErrorExpression internal constructor(
    override val start: Position,
    override val end: Position,
) : Expression {
    override val type: Type get() = ErrorType

    internal constructor(spanned: Spanned) : this(spanned.start, spanned.end)
}

public data class IntegerExpression internal constructor(
    val value: Int,
    override val start: Position,
    override val end: Position,
) : Expression {
    override val type: Type get() = IntegerType
}

public data class StringExpression internal constructor(
    val value: String,
    override val start: Position,
    override val end: Position,
) : Expression {
    override val type: Type get() = StringType
}

public data class BooleanExpression internal constructor(
    val value: Boolean,
    override val start: Position,
    override val end: Position,
) : Expression {
    override val type: Type get() = BooleanType
}

public data class IdentifierExpression internal constructor(
    val symbol: Definition,
    override val type: Type,
    override val start: Position,
    override val end: Position,
) : Expression

public data class FunctionExpression internal constructor(
    val params: List<Parameter>,
    val body: List<Statement>,
    override val type: FunctionType,
    override val start: Position,
    override val end: Position,
) : Expression

public data class Parameter internal constructor(
    val name: String,
    override val type: Type,
    override val start: Position,
    override val end: Position,
) : Typed, Spanned

public data class InvokeExpression internal constructor(
    val target: Expression,
    val substitutedTargetType: Type,
    val args: List<Expression>,
    override val type: Type,
    override val start: Position,
    override val end: Position,
) : Expression
