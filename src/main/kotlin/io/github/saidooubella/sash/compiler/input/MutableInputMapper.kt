package io.github.saidooubella.sash.compiler.input

import io.github.saidooubella.sash.compiler.input.source.InputSource

public inline fun <T, R> MutableInput<T>.map(
    block: (MutableInput<T>) -> InputSource<R>,
): MutableInput<R> = MutableInput(block(this))

public inline fun <R> MutableIntInput.map(
    block: (MutableIntInput) -> InputSource<R>,
): MutableInput<R> = MutableInput(block(this))
