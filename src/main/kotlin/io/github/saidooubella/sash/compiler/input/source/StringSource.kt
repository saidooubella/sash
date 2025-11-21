package io.github.saidooubella.sash.compiler.input.source

import io.github.saidooubella.sash.compiler.utils.isHighSurrogate
import io.github.saidooubella.sash.compiler.utils.isLowSurrogate
import io.github.saidooubella.sash.compiler.utils.toCodePoint

private const val EOF = -1

public class StringSource(private val source: String) : IntInputSource {

    private var index: Int = 0

    override fun isDone(item: Int): Boolean = item == EOF

    override fun next(): Int {

        if (index !in source.indices) {
            return EOF
        }

        val high = source[index++].code

        if (high.isHighSurrogate) {
            check(index in source.indices)
            val low = source[index++].code
            check(low.isLowSurrogate)
            return toCodePoint(high, low)
        }

        return high
    }

    override fun close(): Unit = Unit
}
