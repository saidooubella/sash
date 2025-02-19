package io.github.saidooubella.sash.compiler.utils

internal inline fun fastRepeatAll(times: Int, predicate: (Int) -> Boolean): Boolean {
    for (index in 0..<times) if (!predicate(index)) return false
    return true
}
