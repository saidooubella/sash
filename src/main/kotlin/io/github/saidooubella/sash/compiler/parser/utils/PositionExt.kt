package io.github.saidooubella.sash.compiler.parser.utils

import io.github.saidooubella.sash.compiler.span.Position

@Suppress("NOTHING_TO_INLINE")
internal inline infix fun Position.aligns(that: Position): Boolean {
    return this.line == that.line
}
