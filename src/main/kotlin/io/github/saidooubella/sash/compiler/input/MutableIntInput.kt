package io.github.saidooubella.sash.compiler.input

import io.github.saidooubella.sash.compiler.input.provider.IntInputProvider
import io.github.saidooubella.sash.compiler.utils.IntArrayDeque

public fun MutableIntInput(provider: IntInputProvider): MutableIntInput = MutableIntInputImpl(provider)

public abstract class MutableIntInput @PublishedApi internal constructor() : IntInput() {
    internal abstract fun advance()
}

@PublishedApi
internal fun MutableIntInput.consume(): Int {
    return current.also { advance() }
}

internal fun MutableIntInput.advanceBy(count: Int) {
    repeat(count) { advance() }
}

private class MutableIntInputImpl(
    private val provider: IntInputProvider,
) : MutableIntInput() {

    private val cache = IntArrayDeque()

    override var current = nextElement()
    override var isDone = false

    override fun advance() {
        if (isDone) return
        current = cache.removeFirstOrElse(::nextElement)
    }

    override fun peek(offset: Int): Int {
        require(offset >= 0) { "offset < 0" }
        if (offset == 0 || isDone) return current
        if (offset <= cache.size) return cache[offset - 1]
        repeat(offset - cache.size) {
            val next = provider.next()
            if (provider.isDone(next)) return next
            cache.addLast(next)
        }
        return cache.last()
    }

    override fun close() = provider.close()

    private fun nextElement(): Int {
        val next = provider.next()
        isDone = provider.isDone(next)
        return next
    }
}
