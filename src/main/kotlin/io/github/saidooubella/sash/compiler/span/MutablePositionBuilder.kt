package io.github.saidooubella.sash.compiler.span

public class MutablePositionBuilder : PositionBuilder {

    private var pendingNewLine = false

    private var column = 1
    private var index = 0
    private var line = 1

    private fun onNewLine() {
        column = 1
        line++
    }

    public fun advance(old: Int, new: Int) {

        column++
        index++

        if (pendingNewLine) {
            pendingNewLine = false
            onNewLine()
            return
        }

        when {
            old == '\r'.code && new == '\n'.code -> pendingNewLine = true
            old == '\r'.code || old == '\n'.code -> onNewLine()
        }
    }

    override fun build(): Position = Position(line, column, index)
}
