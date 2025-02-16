package io.github.saidooubella.sash.compiler.tokens.cases.string

import io.github.saidooubella.sash.compiler.input.MutableIntInput
import io.github.saidooubella.sash.compiler.input.advanceBy
import io.github.saidooubella.sash.compiler.input.consume
import io.github.saidooubella.sash.compiler.input.isNotDone
import io.github.saidooubella.sash.compiler.tokens.RawToken
import io.github.saidooubella.sash.compiler.tokens.TokenType
import io.github.saidooubella.sash.compiler.tokens.TokenizerContext
import io.github.saidooubella.sash.compiler.tokens.cases.TokenCase
import io.github.saidooubella.sash.compiler.tokens.utils.consume
import io.github.saidooubella.sash.compiler.tokens.utils.matches
import io.github.saidooubella.sash.compiler.tokens.utils.matchesNewLine
import io.github.saidooubella.sash.compiler.tokens.utils.notMatchesNewLine

internal object StringLiteralCase : TokenCase {

    override fun tryTokenize(context: TokenizerContext, input: MutableIntInput): RawToken? {
        return if (notMatchesEarlyEnd(input)) build(context, input) else quiteStringMode(context)
    }

    private fun build(context: TokenizerContext, input: MutableIntInput): RawToken {

        val start = context.positionBuilder.build()
        var escaping = false

        while (true) {

            if (escaping) {
                handleEscaping(input, context)
                escaping = false
                continue
            }

            if (input.isDone || matchesInvalidPart(input)) {
                break
            }

            if (input.matches('\\')) {
                input.advance()
                escaping = true
                continue
            }

            context.builder.appendCodePoint(input.consume())
        }

        val end = context.positionBuilder.build()
        return RawToken(context.builder.consume(), TokenType.StringLiteral, start, end)
    }

    private fun quiteStringMode(context: TokenizerContext): Nothing? {
        context.exitMode()
        return null
    }

    private val simpleEsc = buildMap {
        put('\\'.code, '\\')
        put('r'.code, '\r')
        put('n'.code, '\n')
        put('t'.code, '\t')
        put('b'.code, '\b')
        put('"'.code, '"')
    }

    private fun handleEscaping(input: MutableIntInput, context: TokenizerContext) {
        if (input.isDone) {
            val position = context.positionBuilder.build()
            context.reporter.reportIncompleteEscaping(position, position)
        } else if (input.matches("\r\n")) {
            input.advanceBy(2)
        } else if (input.matches('\r') || input.matches('\n')) {
            input.advanceBy(1)
        } else if (input.current in simpleEsc) {
            context.builder.append(simpleEsc[input.consume()])
        } else if (input.matches('u') && matchesUnicodeLiteral(input)) {
            input.advance()
            context.builder.appendCodePoint(concatCodepoint(input))
        } else {
            val start = context.positionBuilder.build()
            val char = Character.toChars(input.consume()).concatToString()
            val end = context.positionBuilder.build()
            context.reporter.reportInvalidEscaping(start, end, char)
        }
    }

    private fun concatCodepoint(input: MutableIntInput): Int {
        val u1 = Character.digit(input.consume(), 16)
        val u2 = Character.digit(input.consume(), 16)
        val u3 = Character.digit(input.consume(), 16)
        val u4 = Character.digit(input.consume(), 16)
        return (u1 shl 12) or (u2 shl 8) or (u3 shl 4) or u4
    }

    private fun matchesUnicodeLiteral(input: MutableIntInput): Boolean {
        return 0.rangeUntil(4).all { isHexDigit(input.peek(it)) }
    }

    private fun isHexDigit(codepoint: Int): Boolean {
        return codepoint in '0'.code..'9'.code
                || codepoint in 'A'.code..'Z'.code
                || codepoint in 'a'.code..'z'.code
    }

    private fun matchesInvalidPart(input: MutableIntInput): Boolean {
        return input.matchesNewLine() || input.matches('"')
    }

    private fun notMatchesEarlyEnd(input: MutableIntInput): Boolean {
        return input.isNotDone && input.notMatchesNewLine()
    }
}
