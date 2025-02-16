package io.github.saidooubella.sash.compiler.input

public inline fun MutableIntInput.observer(
    crossinline observer: (old: Int, new: Int) -> Unit,
): MutableIntInput = object : MutableIntInput() {

    private val base = this@observer

    override val current: Int get() = base.current
    override val isDone: Boolean get() = base.isDone

    override fun advance() {
        if (base.isNotDone) observer(base.consume(), base.current)
    }

    override fun peek(offset: Int): Int = base.peek(offset)
    override fun close(): Unit = base.close()
}
