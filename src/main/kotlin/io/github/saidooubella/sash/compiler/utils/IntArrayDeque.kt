package io.github.saidooubella.sash.compiler.utils

internal class IntArrayDeque {

    private var elementData: IntArray = emptyElementData
    private var head: Int = 0

    internal var size: Int = 0
        private set

    private fun ensureCapacity(minCapacity: Int) {
        if (minCapacity < 0) throw IllegalStateException("Deque is too big.")

        if (minCapacity <= elementData.size) return
        if (elementData === emptyElementData) {
            elementData = IntArray(minCapacity.coerceAtLeast(DEFAULT_CAPACITY))
            return
        }

        copyElements(newCapacity(elementData.size, minCapacity))
    }

    private fun copyElements(newCapacity: Int) {
        val newElements = IntArray(newCapacity)
        elementData.copyInto(newElements, 0, head, elementData.size)
        elementData.copyInto(newElements, elementData.size - head, 0, head)
        elementData = newElements
        head = 0
    }

    private fun positiveMod(index: Int): Int {
        return if (index >= elementData.size) index - elementData.size else index
    }

    private fun internalIndex(index: Int): Int = positiveMod(head + index)

    private fun incremented(index: Int): Int = if (index == elementData.size - 1) 0 else index + 1

    private fun isEmpty(): Boolean = size == 0

    internal fun last(): Int {
        if (isEmpty()) throw NoSuchElementException("ArrayDeque is empty.")
        return elementData[internalIndex(size - 1)]
    }

    internal fun addLast(element: Int) {
        ensureCapacity(size + 1)
        elementData[internalIndex(size)] = element
        size += 1
    }

    internal inline fun removeFirstOrElse(default: () -> Int): Int {
        if (isEmpty()) return default()
        val element = elementData[head]
        head = incremented(head)
        size -= 1
        return element
    }

    internal operator fun get(index: Int): Int {
        checkElementIndex(index, size)
        return elementData[internalIndex(index)]
    }
}

private val emptyElementData = IntArray(0)

private const val DEFAULT_CAPACITY = 10
private const val MAX_CAPACITY = Int.MAX_VALUE - 8

private fun newCapacity(oldCapacity: Int, minCapacity: Int): Int {
    var newCapacity = oldCapacity + (oldCapacity shr 1)
    if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity
    if (newCapacity - MAX_CAPACITY > 0)
        newCapacity = if (minCapacity > MAX_CAPACITY) Int.MAX_VALUE else MAX_CAPACITY
    return newCapacity
}

private fun checkElementIndex(index: Int, size: Int) {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index: $index, size: $size")
}
