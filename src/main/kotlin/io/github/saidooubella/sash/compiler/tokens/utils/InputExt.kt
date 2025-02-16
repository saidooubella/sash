package io.github.saidooubella.sash.compiler.tokens.utils

import io.github.saidooubella.sash.compiler.input.IntInput
import io.github.saidooubella.sash.compiler.input.MutableIntInput
import io.github.saidooubella.sash.compiler.input.consume
import io.github.saidooubella.sash.compiler.input.isNotDone
import io.github.saidooubella.sash.compiler.tokens.RawToken
import io.github.saidooubella.sash.compiler.tokens.RawTokenType
import io.github.saidooubella.sash.compiler.tokens.TokenizerContext
import io.github.saidooubella.sash.compiler.utils.forEachCodepoint

internal fun IntInput.matches(char: Char): Boolean {
    return current == char.code
}

internal fun IntInput.matches(text: String): Boolean {
    text.forEachCodepoint { index, codepoint ->
        if (peek(index) != codepoint) return false
    }
    return true
}

internal fun IntInput.matchesWhitespace(): Boolean {
    return Character.isWhitespace(current) && notMatchesNewLine()
}

internal fun IntInput.matchesNewLine(): Boolean {
    return matches('\r') && matches('\n') || matches('\r') || matches('\n')
}

internal fun IntInput.notMatchesNewLine(): Boolean {
    return !matchesNewLine()
}

internal fun MutableIntInput.collect(dest: StringBuilder, count: Int) {
    repeat(count) { dest.appendCodePoint(consume()) }
}

internal inline fun MutableIntInput.collectWhile(
    dest: StringBuilder,
    predicate: (Int) -> Boolean,
) {
    while (isNotDone && predicate(current)) {
        dest.appendCodePoint(consume())
    }
}

internal fun MutableIntInput.consumeCharToken(
    context: TokenizerContext,
    type: RawTokenType,
): RawToken {
    val start = context.positionBuilder.build()
    val character = context.builder.appendCodePoint(consume()).consume()
    val end = context.positionBuilder.build()
    return RawToken(character, type, start, end)
}
