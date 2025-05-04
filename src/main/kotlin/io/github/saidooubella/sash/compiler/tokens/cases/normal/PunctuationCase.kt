package io.github.saidooubella.sash.compiler.tokens.cases.normal

import io.github.saidooubella.sash.compiler.input.MutableIntInput
import io.github.saidooubella.sash.compiler.tokens.RawToken
import io.github.saidooubella.sash.compiler.tokens.RawTokenType
import io.github.saidooubella.sash.compiler.tokens.TokenType
import io.github.saidooubella.sash.compiler.tokens.TokenizerContext
import io.github.saidooubella.sash.compiler.tokens.cases.TokenCase
import io.github.saidooubella.sash.compiler.tokens.utils.collect
import io.github.saidooubella.sash.compiler.tokens.utils.consume
import io.github.saidooubella.sash.compiler.tokens.utils.matches

internal object PunctuationCase : TokenCase {

    override fun tryTokenize(context: TokenizerContext, input: MutableIntInput): RawToken? {
        return punctuations.firstOrNull { input.matches(it.text) }?.let { build(context, input, it) }
    }

    @JvmStatic
    private fun build(context: TokenizerContext, input: MutableIntInput, punctuation: Punctuation): RawToken {
        val start = context.positionBuilder.build()
        input.collect(context.builder, punctuation.text.length)
        val end = context.positionBuilder.build()
        return RawToken(context.builder.consume(), punctuation.type, start, end)
    }

    private class Punctuation(val text: String, val type: RawTokenType)

    private val punctuations = buildList {
        // Two characters punctuation
        add(Punctuation("&&", TokenType.AmpersandAmpersand))
        add(Punctuation(">=", TokenType.GreaterThanEqual))
        add(Punctuation("<=", TokenType.LessThanEqual))
        add(Punctuation("::", TokenType.ColonColon))
        add(Punctuation("==", TokenType.EqualEqual))
        add(Punctuation("!=", TokenType.BangEqual))
        add(Punctuation("||", TokenType.PipePipe))
        add(Punctuation("->", TokenType.Arrow))
        // One-character punctuation
        add(Punctuation("]", TokenType.CloseBracket))
        add(Punctuation("[", TokenType.OpenBracket))
        add(Punctuation(">", TokenType.GreaterThan))
        add(Punctuation(")", TokenType.CloseParent))
        add(Punctuation("(", TokenType.OpenParent))
        add(Punctuation("}", TokenType.CloseBrace))
        add(Punctuation("{", TokenType.OpenBrace))
        add(Punctuation(";", TokenType.SemiColon))
        add(Punctuation("<", TokenType.LessThan))
        add(Punctuation("%", TokenType.Percent))
        add(Punctuation(":", TokenType.Colon))
        add(Punctuation(",", TokenType.Comma))
        add(Punctuation("=", TokenType.Equal))
        add(Punctuation("-", TokenType.Minus))
        add(Punctuation("/", TokenType.Slash))
        add(Punctuation("!", TokenType.Bang))
        add(Punctuation("+", TokenType.Plus))
        add(Punctuation("*", TokenType.Star))
        add(Punctuation(".", TokenType.Dot))
    }
}
