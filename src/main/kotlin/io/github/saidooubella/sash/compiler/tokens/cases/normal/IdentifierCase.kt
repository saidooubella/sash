package io.github.saidooubella.sash.compiler.tokens.cases.normal

import io.github.saidooubella.sash.compiler.input.IntInput
import io.github.saidooubella.sash.compiler.input.MutableIntInput
import io.github.saidooubella.sash.compiler.tokens.RawToken
import io.github.saidooubella.sash.compiler.tokens.TokenType
import io.github.saidooubella.sash.compiler.tokens.TokenizerContext
import io.github.saidooubella.sash.compiler.tokens.cases.TokenCase
import io.github.saidooubella.sash.compiler.tokens.utils.collectWhile
import io.github.saidooubella.sash.compiler.tokens.utils.consume
import io.github.saidooubella.sash.compiler.tokens.utils.matches

internal object IdentifierCase : TokenCase {

    override fun tryTokenize(context: TokenizerContext, input: MutableIntInput): RawToken? {
        return if (input.matchesIdentifierStartChar()) build(context, input) else null
    }

    private fun build(context: TokenizerContext, input: MutableIntInput): RawToken {
        val start = context.positionBuilder.build()
        input.collectWhile(context.builder) { input.matchesIdentifierChar() }
        val end = context.positionBuilder.build()
        val keyword = context.builder.consume()
        return RawToken(keyword, tokenType(keyword), start, end)
    }

    private val keywords = buildMap {
        put("def", TokenType.DefKeyword)
        put("return", TokenType.ReturnKeyword)
        put("false", TokenType.FalseKeyword)
        put("true", TokenType.TrueKeyword)
        put("while", TokenType.WhileKeyword)
        put("break", TokenType.BreakKeyword)
        put("continue", TokenType.ContinueKeyword)
        put("else", TokenType.ElseKeyword)
        put("enum", TokenType.EnumKeyword)
        put("record", TokenType.RecordKeyword)
        put("mut", TokenType.MutKeyword)
        put("if", TokenType.IfKeyword)
        put("_", TokenType.Underscore)
    }

    private fun tokenType(identifier: String) = keywords[identifier] ?: TokenType.Identifier

    private fun IntInput.matchesIdentifierStartChar(): Boolean {
        return matches('_') || Character.isLetter(current)
    }

    private fun IntInput.matchesIdentifierChar(): Boolean {
        return matchesIdentifierStartChar() || Character.isDigit(current)
    }
}
