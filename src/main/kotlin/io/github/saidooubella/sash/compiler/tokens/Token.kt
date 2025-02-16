package io.github.saidooubella.sash.compiler.tokens

import io.github.saidooubella.sash.compiler.span.Position
import io.github.saidooubella.sash.compiler.span.Spanned

public data class RawToken internal constructor(
    val text: String,
    val type: RawTokenType,
    override val start: Position,
    override val end: Position,
) : Spanned

public sealed interface RawTokenType

public data class Token internal constructor(
    val text: String,
    val type: TokenType,
    override val start: Position,
    override val end: Position,
    val leading: List<MetaToken>,
    val trailing: List<MetaToken>,
) : Spanned

public sealed interface TokenType : RawTokenType {
    public object IntegerLiteral : TokenType
    public object DecimalLiteral : TokenType
    public object StringLiteral : TokenType
    public object DoubleQuote : TokenType
    public object SemiColon : TokenType
    public object AmpersandAmpersand : TokenType
    public object PipePipe : TokenType
    public object EqualEqual : TokenType
    public object BangEqual : TokenType
    public object GreaterThanEqual : TokenType
    public object LessThanEqual : TokenType
    public object GreaterThan : TokenType
    public object LessThan : TokenType
    public object Bang : TokenType
    public object Minus : TokenType
    public object Plus : TokenType
    public object Slash : TokenType
    public object Star : TokenType
    public object Percent : TokenType
    public object Equal : TokenType
    public object Colon : TokenType
    public object Comma : TokenType
    public object Arrow : TokenType
    public object Dot : TokenType
    public object OpenBrace : TokenType
    public object CloseBrace : TokenType
    public object OpenBracket : TokenType
    public object Underscore : TokenType
    public object CloseBracket : TokenType
    public object OpenParent : TokenType
    public object CloseParent : TokenType
    public object DefKeyword : TokenType
    public object ReturnKeyword : TokenType
    public object WhileKeyword : TokenType
    public object IfKeyword : TokenType
    public object ElseKeyword : TokenType
    public object RecordKeyword : TokenType
    public object EnumKeyword : TokenType
    public object MutKeyword : TokenType
    public object FalseKeyword : TokenType
    public object TrueKeyword : TokenType
    public object BreakKeyword : TokenType
    public object ContinueKeyword : TokenType
    public object Identifier : TokenType
    public object Injected : TokenType
    public object EndOfFile : TokenType
}

public data class MetaToken internal constructor(
    val text: String,
    val type: MetaTokenType,
    override val start: Position,
    override val end: Position,
) : Spanned

public sealed interface MetaTokenType : RawTokenType {
    public object IllegalCharacter : MetaTokenType
    public object BlockComment : MetaTokenType
    public object LineComment : MetaTokenType
    public object Whitespace : MetaTokenType
    public object LineBreak : MetaTokenType
}
