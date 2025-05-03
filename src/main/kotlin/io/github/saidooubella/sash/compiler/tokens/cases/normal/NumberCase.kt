package io.github.saidooubella.sash.compiler.tokens.cases.normal

import io.github.saidooubella.sash.compiler.input.MutableIntInput
import io.github.saidooubella.sash.compiler.input.consume
import io.github.saidooubella.sash.compiler.tokens.RawToken
import io.github.saidooubella.sash.compiler.tokens.TokenType
import io.github.saidooubella.sash.compiler.tokens.TokenizerContext
import io.github.saidooubella.sash.compiler.tokens.cases.TokenCase
import io.github.saidooubella.sash.compiler.tokens.utils.collectWhile
import io.github.saidooubella.sash.compiler.tokens.utils.consume
import io.github.saidooubella.sash.compiler.tokens.utils.matches

internal object NumberCase : TokenCase {

    override fun tryTokenize(context: TokenizerContext, input: MutableIntInput): RawToken? {
        return if (input.current.isDigit()) build(context, input) else null
    }

    @JvmStatic
    private fun build(context: TokenizerContext, input: MutableIntInput): RawToken {
        val start = context.positionBuilder.build()
        input.collectWhile(context.builder) { it.isDigit() }
        return if (input.matches('.') && input.peek(1).isDigit()) {
            context.builder.appendCodePoint(input.consume())
            input.collectWhile(context.builder) { it.isDigit() }
            val end = context.positionBuilder.build()
            RawToken(context.builder.consume(), TokenType.DecimalLiteral, start, end)
        } else {
            val end = context.positionBuilder.build()
            RawToken(context.builder.consume(), TokenType.IntegerLiteral, start, end)
        }
    }

    @JvmStatic
    private fun Int.isDigit(): Boolean = this in '0'.code..'9'.code
}