package io.github.saidooubella.sash.evaluator.utils

internal inline fun <reified T : Exception, R> catching(transform: (T) -> R, block: () -> R): R {
    return try {
        block()
    } catch (e: Exception) {
        if (e !is T) throw e
        transform(e)
    }
}
