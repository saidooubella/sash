package io.github.saidooubella.sash.evaluator.utils

import kotlin.contracts.contract

internal inline fun <reified T> instanceOf(left: Any, right: Any): Boolean {
    contract { returns(true) implies (left is T && right is T) }
    return left is T && right is T
}

internal inline fun <reified T> checkInstance(value: Any): T & Any {
    contract { returns() implies (value is T) }
    return value as? T ?: error("${value::class.simpleName} is not ${T::class.simpleName}")
}
