package io.github.saidooubella.sash.compiler.utils

import kotlin.math.min

internal inline fun <T, R> List<T>.fastMap(transform: (T) -> R): List<R> {
    val destination = ArrayList<R>(size)
    for (index in indices) destination.add(transform(this[index]))
    return destination
}

internal inline fun <T> List<T>.fastForEach(consumer: (T) -> Unit) {
    for (index in indices) consumer(this[index])
}

internal inline fun <T> List<T>.fastReversedForEach(consumer: (T) -> Unit) {
    for (index in size - 1 downTo 0) consumer(this[index])
}

internal inline fun <T> List<T>.fastJoin(
    separator: String = ", ",
    selector: (T) -> String,
): String = buildString {
    for (index in this@fastJoin.indices) {
        if (index > 0) append(separator)
        append(selector(this@fastJoin[index]))
    }
}

internal inline fun <T, U> List<T>.fastZipEach(that: List<U>, consumer: (T, U) -> Unit) {
    for (index in 0..<min(this.size, that.size)) consumer(this[index], that[index])
}

internal inline fun <K, AK, V> List<K>.fastZipToMutableMap(that: List<V>, keySelector: (K) -> AK): MutableMap<AK, V> {
    val size = min(this.size, that.size)
    val destination = HashMap<AK, V>(size)
    for (index in 0..<size) destination[keySelector(this[index])] = that[index]
    return destination
}
