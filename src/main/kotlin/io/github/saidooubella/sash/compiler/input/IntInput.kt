package io.github.saidooubella.sash.compiler.input

public abstract class IntInput internal constructor() : AutoCloseable {
    @PublishedApi internal abstract val isDone: Boolean
    @PublishedApi internal abstract val current: Int
    @PublishedApi internal abstract fun peek(offset: Int): Int
}

@PublishedApi
internal val IntInput.isNotDone: Boolean
    get() = !isDone
