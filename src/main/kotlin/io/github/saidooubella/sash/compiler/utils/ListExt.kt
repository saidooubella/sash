package io.github.saidooubella.sash.compiler.utils

import kotlin.NoSuchElementException
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

public inline fun <T, R : Any> List<T>.fastFirstNotNullOf(transform: (T) -> R?): R {
    return fastFirstNotNullOfOrNull(transform)
        ?: throw NoSuchElementException("No element of the collection was transformed to a non-null value.")
}

public inline fun <T> List<T>.fastFirstOrNull(predicate: (T) -> Boolean): T? {
    for (index in indices) {
        val element = this[index]
        if (predicate(element)) return element
    }
    return null
}

public inline fun <T, R : Any> List<T>.fastFirstNotNullOfOrNull(transform: (T) -> R?): R? {
    for (index in indices) {
        val result = transform(this[index])
        if (result != null) return result
    }
    return null
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
