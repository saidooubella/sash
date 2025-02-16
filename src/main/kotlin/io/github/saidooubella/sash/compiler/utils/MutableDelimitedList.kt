package io.github.saidooubella.sash.compiler.utils

internal abstract class MutableDelimitedList<E : Any, D : Any> : DelimitedList<E, D>() {
    internal abstract fun addDelimiter(element: D)
    internal abstract fun addElement(element: E)
}

@Suppress("UNCHECKED_CAST")
private class DelimitedListImpl<E : Any, S : Any> : MutableDelimitedList<E, S>() {

    private val items = mutableListOf<Any>()
    private var addElement = true

    override val fullSize: Int get() = items.size

    override fun getDelimiter(index: Int): S = items[index * 2 + 1] as S

    override fun getElement(index: Int): E = items[index * 2] as E

    override fun addElement(element: E) {
        check(addElement) { "A separator must be added" }
        items.add(element)
        addElement = false
    }

    override fun addDelimiter(element: S) {
        check(!addElement) { "An element must be added" }
        items.add(element)
        addElement = true
    }
}

private fun <E : Any, D : Any> mutableDelimitedListOf(): MutableDelimitedList<E, D> = DelimitedListImpl()

internal inline fun <E : Any, D : Any> buildDelimitedList(
    builder: MutableDelimitedList<E, D>.() -> Unit,
): DelimitedList<E, D> = mutableDelimitedListOf<E, D>().apply(builder)
