package io.github.saidooubella.sash.evaluator.utils

internal inline fun <reified E : Exception, R> catching(transform: (E) -> R, block: () -> R): R {
    return try {
        block()
    } catch (e: Exception) {
        if (e !is E) throw e
        transform(e)
    }
}
