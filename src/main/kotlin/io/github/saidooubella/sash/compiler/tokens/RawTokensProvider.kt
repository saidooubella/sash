package io.github.saidooubella.sash.compiler.tokens

import io.github.saidooubella.sash.compiler.input.MutableIntInput
import io.github.saidooubella.sash.compiler.input.provider.InputProvider
import io.github.saidooubella.sash.compiler.tokens.cases.normal.*
import io.github.saidooubella.sash.compiler.tokens.cases.string.StringLiteralCase
import io.github.saidooubella.sash.compiler.tokens.cases.string.StringUnquoteCase
import io.github.saidooubella.sash.compiler.utils.fastFirstNotNullOf
import io.github.saidooubella.sash.compiler.utils.fastFirstNotNullOfOrNull

private val normalModeCases = buildList {
    add(EndOfFileCase)
    add(BlockCommentCase)
    add(LineCommentCase)
    add(LineBreakCase)
    add(WhitespaceCase)
    add(IdentifierCase)
    add(NumberCase)
    add(StringQuoteCase)
    add(PunctuationCase)
    add(IllegalCharacterCase)
}

private val stringModeCases = buildList {
    add(StringUnquoteCase)
    add(StringLiteralCase)
}

public class RawTokensProvider(
    private val input: MutableIntInput,
    private val context: TokenizerContext,
) : InputProvider<RawToken> {

    override fun next(): RawToken {
        while (true) {
            return when (context.currentMode) {
                TokenizerMode.Normal -> normalModeToken()
                TokenizerMode.String -> stringModeToken()
            } ?: continue
        }
    }

    private fun stringModeToken(): RawToken? {
        return stringModeCases.fastFirstNotNullOfOrNull { it.tryTokenize(context, input) }
    }

    private fun normalModeToken(): RawToken {
        return normalModeCases.fastFirstNotNullOf { it.tryTokenize(context, input) }
    }

    override fun isDone(item: RawToken): Boolean {
        return item.type == TokenType.EndOfFile
    }

    override fun close(): Unit = input.close()
}
