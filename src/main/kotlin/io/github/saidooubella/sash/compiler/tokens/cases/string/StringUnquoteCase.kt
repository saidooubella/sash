package io.github.saidooubella.sash.compiler.tokens.cases.string

import io.github.saidooubella.sash.compiler.input.MutableIntInput
import io.github.saidooubella.sash.compiler.tokens.RawToken
import io.github.saidooubella.sash.compiler.tokens.TokenType
import io.github.saidooubella.sash.compiler.tokens.TokenizerContext
import io.github.saidooubella.sash.compiler.tokens.cases.TokenCase
import io.github.saidooubella.sash.compiler.tokens.utils.consumeCharToken
import io.github.saidooubella.sash.compiler.tokens.utils.matches

internal object StringUnquoteCase : TokenCase {

    override fun tryTokenize(context: TokenizerContext, input: MutableIntInput): RawToken? {
        return if (input.matches('"')) build(context, input) else null
    }

    @JvmStatic
    private fun build(context: TokenizerContext, input: MutableIntInput): RawToken {
        context.exitMode()
        return input.consumeCharToken(context, TokenType.DoubleQuote)
    }
}
