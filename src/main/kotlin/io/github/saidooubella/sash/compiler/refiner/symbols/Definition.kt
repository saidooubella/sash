package io.github.saidooubella.sash.compiler.refiner.symbols

public data class Definition internal constructor(
    public val name: String,
    override val type: Type,
    public val readonly: Boolean,
) : Typed
