package io.github.saidooubella.sash.compiler.input.provider

import kotlinx.io.Source
import kotlinx.io.readCodePointValue

private const val EOF = -1

public class SourceProvider(private val source: Source) : IntInputProvider {
    override fun next(): Int = tryOrElse(EOF) { source.readCodePointValue() }
    override fun isDone(item: Int): Boolean = item == EOF
    override fun close(): Unit = source.close()
}

@Suppress("SameParameterValue")
private inline fun tryOrElse(default: Int, block: () -> Int): Int {
    return try { block() } catch (_: Exception) { default }
}
