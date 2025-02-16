package io.github.saidooubella.sash.compiler.tokens

import io.github.saidooubella.sash.compiler.input.MutableIntInput
import io.github.saidooubella.sash.compiler.input.provider.InputProvider
import io.github.saidooubella.sash.compiler.tokens.cases.normal.*
import io.github.saidooubella.sash.compiler.tokens.cases.string.StringLiteralCase
import io.github.saidooubella.sash.compiler.tokens.cases.string.StringUnquoteCase

private val normalModeCases = listOf(
    EndOfFileCase,
    BlockCommentCase,
    LineCommentCase,
    LineBreakCase,
    WhitespaceCase,
    IdentifierCase,
    NumberCase,
    StringQuoteCase,
    PunctuationCase,
    IllegalCharacterCase,
)

private val stringModeCases = listOf(
    StringUnquoteCase,
    StringLiteralCase,
)

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
        return stringModeCases.firstNotNullOfOrNull { it.tryTokenize(context, input) }
    }

    private fun normalModeToken(): RawToken {
        return normalModeCases.firstNotNullOf { it.tryTokenize(context, input) }
    }

    override fun isDone(item: RawToken): Boolean {
        return item.type == TokenType.EndOfFile
    }

    override fun close(): Unit = input.close()
}
