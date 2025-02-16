package io.github.saidooubella.sash.compiler.tokens.cases.normal

import io.github.saidooubella.sash.compiler.input.MutableIntInput
import io.github.saidooubella.sash.compiler.tokens.MetaTokenType
import io.github.saidooubella.sash.compiler.tokens.RawToken
import io.github.saidooubella.sash.compiler.tokens.TokenizerContext
import io.github.saidooubella.sash.compiler.tokens.cases.TokenCase
import io.github.saidooubella.sash.compiler.tokens.utils.consumeCharToken

internal object IllegalCharacterCase : TokenCase {
    override fun tryTokenize(context: TokenizerContext, input: MutableIntInput): RawToken {
        val token = input.consumeCharToken(context, MetaTokenType.IllegalCharacter)
        context.reporter.reportIllegalCharacter(token.start, token.end, token.text)
        return token
    }
}
