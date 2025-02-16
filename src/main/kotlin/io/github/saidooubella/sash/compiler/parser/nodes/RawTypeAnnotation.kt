package io.github.saidooubella.sash.compiler.parser.nodes

import io.github.saidooubella.sash.compiler.span.Position
import io.github.saidooubella.sash.compiler.span.Spanned
import io.github.saidooubella.sash.compiler.tokens.Token
import io.github.saidooubella.sash.compiler.utils.DelimitedList

public data class RawTypeAnnotation internal constructor(
    val colon: Token,
    val type: RawType,
) : Spanned {
    override val start: Position get() = colon.start
    override val end: Position get() = type.end
}

public data class TypeRawArgs internal constructor(
    val openBracket: Token,
    val args: DelimitedList<RawType, Token>,
    val closeBracket: Token,
) : Spanned {
    override val start: Position get() = openBracket.start
    override val end: Position get() = closeBracket.end
}

public sealed interface RawType : Spanned

public data class RawSimpleType internal constructor(
    val name: Token,
    val typeArgs: TypeRawArgs?,
) : RawType {
    override val start: Position get() = name.start
    override val end: Position get() = typeArgs?.end ?: name.end
}

public data class RawFunctionType internal constructor(
    val openParent: Token,
    val valueParams: DelimitedList<RawType, Token>,
    val closeParent: Token,
    val arrow: Token,
    val returnType: RawType,
) : RawType {
    override val start: Position get() = openParent.start
    override val end: Position get() = returnType.end
}
