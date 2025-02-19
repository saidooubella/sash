package io.github.saidooubella.sash.compiler.tokens.cases.normal

import io.github.saidooubella.sash.compiler.input.MutableIntInput
import io.github.saidooubella.sash.compiler.tokens.MetaTokenType
import io.github.saidooubella.sash.compiler.tokens.RawToken
import io.github.saidooubella.sash.compiler.tokens.TokenizerContext
import io.github.saidooubella.sash.compiler.tokens.cases.TokenCase
import io.github.saidooubella.sash.compiler.tokens.utils.collectWhile
import io.github.saidooubella.sash.compiler.tokens.utils.consume
import io.github.saidooubella.sash.compiler.tokens.utils.matchesWhitespace

internal object WhitespaceCase : TokenCase {

    override fun tryTokenize(context: TokenizerContext, input: MutableIntInput): RawToken? {
        return if (input.matchesWhitespace()) build(context, input) else null
    }

    @JvmStatic
    private fun build(context: TokenizerContext, input: MutableIntInput): RawToken {
        val start = context.positionBuilder.build()
        input.collectWhile(context.builder) { input.matchesWhitespace() }
        val end = context.positionBuilder.build()
        return RawToken(context.builder.consume(), MetaTokenType.Whitespace, start, end)
    }
}
