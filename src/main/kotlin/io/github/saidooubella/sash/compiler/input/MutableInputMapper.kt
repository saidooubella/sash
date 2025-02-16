package io.github.saidooubella.sash.compiler.input

import io.github.saidooubella.sash.compiler.input.provider.InputProvider

public inline fun <T, R> MutableInput<T>.map(
    block: (MutableInput<T>) -> InputProvider<R>,
): MutableInput<R> = MutableInput(block(this))

public inline fun <R> MutableIntInput.map(
    block: (MutableIntInput) -> InputProvider<R>,
): MutableInput<R> = MutableInput(block(this))
