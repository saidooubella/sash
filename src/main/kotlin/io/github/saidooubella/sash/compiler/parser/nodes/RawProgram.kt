package io.github.saidooubella.sash.compiler.parser.nodes

import io.github.saidooubella.sash.compiler.tokens.Token

public data class RawProgram internal constructor(
    val statements: List<RawStatement>,
    val endOfFile: Token,
)
