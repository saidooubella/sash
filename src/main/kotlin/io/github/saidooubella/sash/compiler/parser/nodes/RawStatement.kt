package io.github.saidooubella.sash.compiler.parser.nodes

import io.github.saidooubella.sash.compiler.span.Position
import io.github.saidooubella.sash.compiler.span.Spanned
import io.github.saidooubella.sash.compiler.tokens.Token
import io.github.saidooubella.sash.compiler.utils.DelimitedList

public sealed interface RawStatement : Spanned

public data class DefinitionRawStatement internal constructor(
    val defKeyword: Token,
    val mutKeyword: Token?,
    val identifier: Token,
    val typeParams: RawTypeParameters?,
    val typeAnnotation: RawTypeAnnotation?,
    val equal: Token,
    val initializer: RawInitializer,
    val semi: Token,
) : RawStatement {
    override val start: Position get() = defKeyword.start
    override val end: Position get() = initializer.end
}

public sealed interface RawInitializer : Spanned

public data class EnumRawInitializer internal constructor(
    val enumKeyword: Token,
    val openBrace: Token,
    val entries: DelimitedList<EnumRawEntry, Token>,
    val closeBrace: Token,
) : RawInitializer {
    override val start: Position get() = enumKeyword.start
    override val end: Position get() = closeBrace.end
}

public data class EnumRawEntry internal constructor(
    val identifier: Token,
    val fields: RawFields?,
) : Spanned {
    override val start: Position get() = identifier.start
    override val end: Position get() = fields?.end ?: identifier.end
}

public data class RecordRawInitializer internal constructor(
    val recordKeyword: Token,
    val fields: RawFields?,
) : RawInitializer {
    override val start: Position get() = recordKeyword.start
    override val end: Position get() = fields?.end ?: recordKeyword.end
}

public data class RawFields internal constructor(
    val openBrace: Token,
    val fields: DelimitedList<RawField, Token>,
    val closeBrace: Token,
) : Spanned {
    override val start: Position get() = openBrace.start
    override val end: Position get() = closeBrace.end
}

public data class RawField internal constructor(
    val identifier: Token,
    val colon: Token,
    val type: RawType,
) : Spanned {
    override val start: Position get() = identifier.start
    override val end: Position get() = type.end
}

public data class ExpressionRawInitializer internal constructor(
    val expression: RawExpression,
) : RawInitializer {
    override val start: Position get() = expression.start
    override val end: Position get() = expression.end
}

public data class RawTypeParameters internal constructor(
    val openBracket: Token,
    val typeParams: DelimitedList<Token, Token>,
    val closeBracket: Token,
) : Spanned {
    override val start: Position get() = openBracket.start
    override val end: Position get() = closeBracket.end
}

public data class ReturnRawStatement internal constructor(
    val returnKeyword: Token,
    val expression: RawExpression?,
    val semi: Token,
) : RawStatement {
    override val start: Position get() = returnKeyword.start
    override val end: Position get() = expression?.end ?: returnKeyword.end
}

public data class ImplicitResultRawStatement internal constructor(
    val expression: RawExpression,
) : RawStatement {
    override val start: Position get() = expression.start
    override val end: Position get() = expression.end
}

public data class EmptyRawStatement internal constructor(
    val semi: List<Token>,
    override val start: Position,
    override val end: Position,
) : RawStatement

public data class ContinueRawStatement internal constructor(
    val continueKeyword: Token,
    val semi: Token,
) : RawStatement {
    override val start: Position get() = continueKeyword.start
    override val end: Position get() = semi.end
}

public data class BreakRawStatement internal constructor(
    val breakKeyword: Token,
    val semi: Token,
) : RawStatement {
    override val start: Position get() = breakKeyword.start
    override val end: Position get() = semi.end
}

public data class ExpressionRawStatement internal constructor(
    val expression: RawExpression,
    val semi: Token?,
) : RawStatement {
    override val start: Position get() = expression.start
    override val end: Position get() = semi?.end ?: expression.end
}

public data class WhileRawStatement internal constructor(
    val whileKeyword: Token,
    val condition: RawExpression,
    val body: RawControlBody,
) : RawStatement {
    override val start: Position get() = whileKeyword.start
    override val end: Position get() = body.end
}

public data class DropStatement internal constructor(
    val underscore: Token,
    val equal: Token,
    val expression: RawExpression,
    val semi: Token,
) : RawStatement {
    override val start: Position get() = underscore.start
    override val end: Position get() = semi.end
}
