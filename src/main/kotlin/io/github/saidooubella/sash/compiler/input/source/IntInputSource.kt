package io.github.saidooubella.sash.compiler.input.source

public interface IntInputSource : AutoCloseable {
	public fun isDone(item: Int): Boolean
	public fun next(): Int
}
