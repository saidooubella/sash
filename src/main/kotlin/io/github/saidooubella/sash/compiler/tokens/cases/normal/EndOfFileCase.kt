package io.github.saidooubella.sash.compiler.tokens.cases.normal

import io.github.saidooubella.sash.compiler.input.MutableIntInput
import io.github.saidooubella.sash.compiler.tokens.RawToken
import io.github.saidooubella.sash.compiler.tokens.TokenType
import io.github.saidooubella.sash.compiler.tokens.TokenizerContext
import io.github.saidooubella.sash.compiler.tokens.cases.TokenCase

internal object EndOfFileCase : TokenCase {

    override fun tryTokenize(context: TokenizerContext, input: MutableIntInput): RawToken? {
        return if (input.isDone) build(context) else null
    }

    private fun build(context: TokenizerContext): RawToken {
        val position = context.positionBuilder.build()
        return RawToken("end of file", TokenType.EndOfFile, position, position)
    }
}