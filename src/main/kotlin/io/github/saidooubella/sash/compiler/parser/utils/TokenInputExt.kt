package io.github.saidooubella.sash.compiler.parser.utils

import io.github.saidooubella.sash.compiler.input.MutableInput
import io.github.saidooubella.sash.compiler.input.consume
import io.github.saidooubella.sash.compiler.input.isNotDone
import io.github.saidooubella.sash.compiler.parser.context.ParserContext
import io.github.saidooubella.sash.compiler.tokens.Token
import io.github.saidooubella.sash.compiler.tokens.TokenType

internal fun MutableInput<Token>.mark(context: ParserContext) {
    context.canReportErrors = true
    context.reporter.draft()
    mark()
}

internal fun MutableInput<Token>.rewind(context: ParserContext) {
    context.canReportErrors = true
    context.reporter.discard()
    rewind()
}

internal fun MutableInput<Token>.done(context: ParserContext) {
    context.reporter.retain()
    done()
}

internal fun MutableInput<Token>.consumeToken(
    context: ParserContext,
    type: TokenType,
    expected: String,
): Token {

    if (current.type == type) {
        context.canReportErrors = true
        return consume()
    }

    if (context.canReportErrors) {
        val (actual, _, start, end) = current
        context.reporter.reportUnexpectedToken(start, end, expected, actual)
        context.canReportErrors = false
    }

    return Token("", TokenType.Injected, current.start, current.start, listOf(), listOf())
}

internal fun MutableInput<Token>.consumeTokenOrNull(context: ParserContext, type: TokenType): Token? {
    return when (current.type == type) {
        true -> consume().also { context.canReportErrors = true }
        else -> null
    }
}

internal inline fun MutableInput<Token>.consumeWhile(
    context: ParserContext,
    predicate: () -> Boolean = { true },
    block: () -> Unit,
) {
    while (isNotDone && predicate()) {
        val start = current.also { block() }
        if (!context.canReportErrors && start === current) advance()
    }
}
