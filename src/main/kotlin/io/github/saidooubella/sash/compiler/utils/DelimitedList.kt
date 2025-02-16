package io.github.saidooubella.sash.compiler.utils

public abstract class DelimitedList<out E : Any, out D : Any> internal constructor() {
    public abstract val fullSize: Int
    public abstract fun getDelimiter(index: Int): D
    public abstract fun getElement(index: Int): E
}

private object EmptyDelimitedList : DelimitedList<Nothing, Nothing>() {
    override val fullSize: Int = 0
    override fun getDelimiter(index: Int): Nothing = throw IndexOutOfBoundsException()
    override fun getElement(index: Int): Nothing = throw IndexOutOfBoundsException()
}

internal fun <E : Any, D : Any> emptyDelimitedList(): DelimitedList<E, D> = EmptyDelimitedList

internal fun <E : Any, D : Any> DelimitedList<E, D>?.orEmpty(): DelimitedList<E, D> {
    return this ?: EmptyDelimitedList
}

internal inline val <E : Any, D : Any> DelimitedList<E, D>.elementsSize: Int
    get() = (fullSize + 1) / 2

internal inline fun <T : Any> DelimitedList<T, *>.forEach(consumer: (T) -> Unit) {
    for (index in 0..<elementsSize) consumer(getElement(index))
}

internal inline fun <T : Any> DelimitedList<T, *>.forEachIndexed(consumer: (Int, T) -> Unit) {
    for (index in 0..<elementsSize) consumer(index, getElement(index))
}

internal inline fun <T : Any, R> DelimitedList<T, *>.map(transform: (T) -> R): List<R> {
    val destination = ArrayList<R>(elementsSize)
    for (index in 0..<elementsSize) destination.add(transform(getElement(index)))
    return destination
}

internal inline fun <T : Any, R> DelimitedList<T, *>.mapNotNull(transform: (T) -> R): List<R & Any> {
    val destination = ArrayList<R & Any>(elementsSize)
    for (index in 0..<elementsSize) {
        val element = transform(getElement(index))
        if (element != null) destination.add(element)
    }
    return destination
}
