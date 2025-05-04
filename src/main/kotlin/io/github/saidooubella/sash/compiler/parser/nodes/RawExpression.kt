package io.github.saidooubella.sash.compiler.parser.nodes

import io.github.saidooubella.sash.compiler.span.Position
import io.github.saidooubella.sash.compiler.span.Spanned
import io.github.saidooubella.sash.compiler.tokens.Token
import io.github.saidooubella.sash.compiler.utils.DelimitedList

public sealed interface RawExpression : Spanned

public data class AccessRawExpression internal constructor(
    val target: RawExpression,
    val dot: Token,
    val identifier: Token,
) : RawExpression {
    override val start: Position get() = target.start
    override val end: Position get() = identifier.end
}

public data class IfRawExpression internal constructor(
    val ifKeyword: Token,
    val condition: RawExpression,
    val body: RawControlBody,
    val elseIfClauses: List<ElseIfRawClause>,
    val elseClause: ElseRawClause?,
) : RawExpression {
    override val start: Position get() = ifKeyword.start
    override val end: Position get() = elseClause?.end ?: elseIfClauses.lastOrNull()?.end ?: body.end
}

public data class ElseIfRawClause internal constructor(
    val elseKeyword: Token,
    val ifKeyword: Token,
    val condition: RawExpression,
    val body: RawControlBody,
) : Spanned {
    override val start: Position get() = elseKeyword.start
    override val end: Position get() = body.end
}

public data class ElseRawClause internal constructor(
    val elseKeyword: Token,
    val body: RawControlBody,
) : Spanned {
    override val start: Position get() = elseKeyword.start
    override val end: Position get() = body.end
}

public data class ParenthesizedRawExpression internal constructor(
    val openParent: Token,
    val expression: RawExpression,
    val closeParent: Token,
) : RawExpression {
    override val start: Position get() = openParent.start
    override val end: Position get() = closeParent.end
}

public data class AssignmentRawExpression internal constructor(
    val target: RawExpression,
    val assign: Token,
    val value: RawExpression,
) : RawExpression {
    override val start: Position get() = target.start
    override val end: Position get() = value.end
}

public data class UnaryRawExpression internal constructor(
    val operator: Token,
    val operand: RawExpression,
) : RawExpression {
    override val start: Position get() = operator.start
    override val end: Position get() = operand.end
}

public data class LogicalBinaryRawExpression internal constructor(
    val left: RawExpression,
    val operator: Token,
    val right: RawExpression,
) : RawExpression {
    override val start: Position get() = left.start
    override val end: Position get() = right.end
}

public data class BinaryRawExpression internal constructor(
    val left: RawExpression,
    val operator: Token,
    val right: RawExpression,
) : RawExpression {
    override val start: Position get() = left.start
    override val end: Position get() = right.end
}

public data class DecimalRawExpression internal constructor(
    val value: Token,
) : RawExpression {
    override val start: Position get() = value.start
    override val end: Position get() = value.end
}

public data class IdentifierRawExpression internal constructor(
    val identifier: Token,
) : RawExpression {
    override val start: Position get() = identifier.start
    override val end: Position get() = identifier.end
}

public data class IntegerRawExpression internal constructor(
    val value: Token,
) : RawExpression {
    override val start: Position get() = value.start
    override val end: Position get() = value.end
}

public data class StringRawExpression internal constructor(
    val quote: Token,
    val value: Token?,
    val unquote: Token?,
) : RawExpression {
    override val start: Position get() = quote.start
    override val end: Position get() = unquote?.end ?: value?.end ?: quote.end
}

public data class BooleanRawExpression internal constructor(
    val value: Token,
) : RawExpression {
    override val start: Position get() = value.start
    override val end: Position get() = value.end
}

public data class FunctionRawExpression internal constructor(
    val openBrace: Token,
    val params: FunctionRawSignature?,
    val statements: List<RawStatement>,
    val closeBrace: Token,
) : RawExpression {
    override val start: Position get() = openBrace.start
    override val end: Position get() = closeBrace.end
}

public data class FunctionRawSignature internal constructor(
    val params: DelimitedList<RawParameter, Token>,
    val returnType: FunctionRawReturnType?,
    val arrow: Token,
)

public data class FunctionRawReturnType internal constructor(
    val colonColon: Token,
    val type: RawType,
) : Spanned {
    override val start: Position get() = colonColon.start
    override val end: Position get() = type.end
}

public data class RawParameter internal constructor(
    val identifier: Token,
    val type: RawTypeAnnotation?,
) : Spanned {
    override val start: Position get() = identifier.start
    override val end: Position get() = type?.end ?: identifier.end
}

public data class InvokeRawExpression internal constructor(
    val target: RawExpression,
    val typeArgs: TypeRawArgs?,
    val valueArgs: ValueRawArgs,
) : RawExpression {
    override val start: Position get() = target.start
    override val end: Position get() = valueArgs.end
}
