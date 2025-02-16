package io.github.saidooubella.sash.compiler.span

public data class Position internal constructor(
    val line: Int,
    val column: Int,
    val index: Int,
)
