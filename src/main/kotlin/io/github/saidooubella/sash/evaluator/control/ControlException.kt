package io.github.saidooubella.sash.evaluator.control

import io.github.saidooubella.sash.evaluator.values.AnyValue

internal abstract class ControlException : Exception() {

    internal class Return(val value: AnyValue) : ControlException()

    internal class Yield(val value: AnyValue) : ControlException()

    internal object Continue : ControlException() {
        @Suppress("unused")
        private fun readResolve(): Any = Continue
    }

    internal object Break : ControlException() {
        @Suppress("unused")
        private fun readResolve(): Any = Break
    }
}
