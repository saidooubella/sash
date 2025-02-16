package io.github.saidooubella.sash.compiler.diagnostics

import io.github.saidooubella.sash.compiler.span.Position
import io.github.saidooubella.sash.compiler.span.Spanned

public data class Diagnostic internal constructor(
    val fileName: String,
    val message: String,
    override val start: Position,
    override val end: Position,
) : Spanned

public val Diagnostic.formattedMessage: String
    get() = buildString {
        append('(')
        append(fileName)
        append(':')
        append(start.line)
        append(':')
        append(start.column)
        append(')')
        append(' ')
        append(message)
    }
