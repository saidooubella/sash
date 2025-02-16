package io.github.saidooubella.sash.evaluator

import io.github.saidooubella.sash.compiler.refiner.symbols.Definition
import io.github.saidooubella.sash.compiler.refiner.symbols.Exit
import io.github.saidooubella.sash.compiler.refiner.symbols.PrintLn
import io.github.saidooubella.sash.compiler.refiner.symbols.TypeName
import io.github.saidooubella.sash.evaluator.utils.checkInstance
import io.github.saidooubella.sash.evaluator.values.*
import kotlin.system.exitProcess

public class Environment(private val parent: Environment? = null) {

    private val values = HashMap<String, AnyValue>()

    public fun create(name: String, value: AnyValue) {
        values[name] = value
    }

    internal fun update(name: String, value: AnyValue) {
        var current: Environment? = this
        while (current != null) {
            if (name in current.values) {
                current.values[name] = value
                return
            }
            current = current.parent
        }
        error("`$name` was not found for update")
    }

    internal fun get(name: String): AnyValue {
        var current: Environment? = this
        while (current != null) {
            val value = current.values[name]
            if (value != null) return value
            current = current.parent
        }
        error("`$name` was not found")
    }
}

internal fun builtInEnvironment(): Environment = Environment().apply {
    createNativeFunction(PrintLn) { println(it[0]); UnitValue }
    createNativeFunction(Exit) { exitProcess(checkInstance<IntegerValue>(it[0]).value) }
    createNativeFunction(TypeName) { StringValue(it[0].type.stringify()) }
}

public fun Environment.createNativeFunction(symbol: Definition, block: (List<AnyValue>) -> AnyValue) {
    create(symbol.name, NativeFunction(symbol.type, block))
}
