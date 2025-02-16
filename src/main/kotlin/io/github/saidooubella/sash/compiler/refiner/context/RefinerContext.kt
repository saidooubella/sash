package io.github.saidooubella.sash.compiler.refiner.context

import io.github.saidooubella.sash.compiler.diagnostics.DiagnosticsReporter
import io.github.saidooubella.sash.compiler.refiner.hasPlaceholders
import io.github.saidooubella.sash.compiler.refiner.symbols.Definition
import io.github.saidooubella.sash.compiler.refiner.symbols.NamedType
import io.github.saidooubella.sash.compiler.refiner.symbols.Type
import io.github.saidooubella.sash.compiler.utils.fastReversedForEach
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public class RefinerContext(
    internal val reporter: DiagnosticsReporter,
) {
    private val contextualTypes = ArrayDeque<Type?>()
    private val scopes = ArrayDeque<ContextualScope>()

    private val symbols: ArrayDeque<HashMap<String, Definition>> = ArrayDeque()
    private val types: ArrayDeque<HashMap<String, NamedType>> = ArrayDeque()

    init {
        symbols.addLast(HashMap())
        types.addLast(HashMap())
    }

    // ---------------------------------------------------------------------------------------------

    internal fun putDefinition(definition: Definition) {
        symbols.last()[definition.name] = definition
    }

    internal fun putType(type: NamedType) {
        types.last()[type.name] = type
    }

    internal fun hasDefinition(name: String): Boolean = symbols.last().contains(name)

    internal fun getDefinition(name: String): Definition? {
        symbols.fastReversedForEach { scope ->
            return scope[name] ?: return@fastReversedForEach
        }
        return null
    }

    internal fun getType(name: String): Type? {
        types.fastReversedForEach { scope ->
            return scope[name] ?: return@fastReversedForEach
        }
        return null
    }

    // ---------------------------------------------------------------------------------------------

    internal inline fun <T> scoped(block: () -> T): T {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        return types.using(HashMap()) { symbols.using(HashMap(), block) }
    }

    internal inline fun <T> scoped(scope: ContextualScope, block: () -> T): T {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        return scoped { scopes.using(scope, block) }
    }

    internal inline fun <reified T : ContextualScope> currentScope(): T? {
        scopes.fastReversedForEach {
            if (it is T) return it else if (it.terminal) return null
        }
        return null
    }

    // ---------------------------------------------------------------------------------------------

    internal inline fun <T> withContextualType(type: Type?, block: () -> T): T {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        return contextualTypes.using(type, block)
    }

    internal inline fun <reified T : Type> currentContextualType(): T? {
        return contextualTypes.lastOrNull() as? T
    }

    // ---------------------------------------------------------------------------------------------
}

private inline fun <T, R> ArrayDeque<T>.using(element: T, block: () -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    try {
        addLast(element)
        return block()
    } finally {
        removeLast()
    }
}

internal sealed class ContextualScope {
    internal abstract val terminal: Boolean
}

internal class FunctionScope(returnType: Type?) : ContextualScope() {

    internal var returnType: Type? = returnType
        set(value) {
            val current = field?.takeUnless { it.hasPlaceholders() }
            if (current == null) field = value
        }

    override val terminal: Boolean get() = true
}

internal object LoopScope : ContextualScope() {
    override val terminal: Boolean get() = false
}

internal class IfScope(returnType: Type?) : ContextualScope() {

    internal var yieldedType: Type? = returnType
        set(value) {
            val current = field?.takeUnless { it.hasPlaceholders() }
            if (current == null) field = value
        }

    override val terminal: Boolean get() = false
}
