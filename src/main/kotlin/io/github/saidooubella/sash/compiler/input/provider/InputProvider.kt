package io.github.saidooubella.sash.compiler.input.provider

public interface InputProvider<T> : AutoCloseable {
	public fun isDone(item: T): Boolean
	public fun next(): T
}
