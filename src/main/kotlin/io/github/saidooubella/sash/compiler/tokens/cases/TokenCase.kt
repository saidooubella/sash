package io.github.saidooubella.sash.compiler.tokens.cases

import io.github.saidooubella.sash.compiler.input.MutableIntInput
import io.github.saidooubella.sash.compiler.tokens.RawToken
import io.github.saidooubella.sash.compiler.tokens.TokenizerContext

internal interface TokenCase {
    fun tryTokenize(context: TokenizerContext, input: MutableIntInput): RawToken?
}
