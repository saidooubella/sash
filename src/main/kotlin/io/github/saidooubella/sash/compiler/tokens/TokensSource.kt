package io.github.saidooubella.sash.compiler.tokens

import io.github.saidooubella.sash.compiler.input.MutableInput
import io.github.saidooubella.sash.compiler.input.consume
import io.github.saidooubella.sash.compiler.input.isNotDone
import io.github.saidooubella.sash.compiler.input.source.InputSource

public class TokensSource(
    private val input: MutableInput<RawToken>,
) : InputSource<Token> {

    override fun isDone(item: Token): Boolean = item.type == TokenType.EndOfFile

    override fun next(): Token {
        val leading = metaTokens(input, false)
        val token = input.consume()
        val trailing = metaTokens(input, true)
        return token.run { Token(text, type as TokenType, start, end, leading, trailing) }
    }

    override fun close(): Unit = input.close()
}

private fun metaTokens(input: MutableInput<RawToken>, isTrailing: Boolean) = buildList {
    while (input.isNotDone && input.current.type is MetaTokenType) {
        val token = input.consume()
        add(MetaToken(token.text, token.type as MetaTokenType, token.start, token.end))
        if (isTrailing && token.type == MetaTokenType.LineBreak) break
    }
}
