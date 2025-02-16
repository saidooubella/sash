@file:Suppress("NOTHING_TO_INLINE")

package io.github.saidooubella.sash.compiler.utils

internal inline val Int.isHighSurrogate: Boolean
    get() = this in 0xD800..0xDBFF

internal inline val Int.isLowSurrogate: Boolean
    get() = this in 0xDC00..0xDFFF

internal inline fun toCodePoint(high: Int, low: Int): Int {
    return ((high - 0xD800 shl 10) or (low - 0xDC00)) + 0x10000
}

internal inline fun String.forEachCodepoint(action: (index: Int, codepoint: Int) -> Unit) {

    var cpIndex = 0
    var index = 0

    while (index in indices) {

        var high = this[index++].code

        if (high.isHighSurrogate) {
            check(index in indices)
            val low = this[index++].code
            check(low.isLowSurrogate)
            high = toCodePoint(high, low)
        }

        action(cpIndex++, high)
    }
}
