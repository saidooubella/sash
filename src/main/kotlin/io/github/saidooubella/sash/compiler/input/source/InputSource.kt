package io.github.saidooubella.sash.compiler.input.source

public interface InputSource<T> : AutoCloseable {
	public fun isDone(item: T): Boolean
	public fun next(): T
}
