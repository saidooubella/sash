package io.github.saidooubella.sash.evaluator.values

import io.github.saidooubella.sash.compiler.refiner.nodes.Parameter
import io.github.saidooubella.sash.compiler.refiner.nodes.Statement
import io.github.saidooubella.sash.compiler.refiner.symbols.*
import io.github.saidooubella.sash.compiler.utils.fastForEach
import io.github.saidooubella.sash.compiler.utils.fastZipEach
import io.github.saidooubella.sash.evaluator.Environment
import io.github.saidooubella.sash.evaluator.evalStatement

internal typealias AnyValue = Value<*>

public abstract class Value<Self : Value<Self>> : Typed {

    internal abstract fun eq(that: Self): BooleanValue
    internal abstract fun stringify(): StringValue

    override fun toString(): String = stringify().value
}

@Suppress("UNCHECKED_CAST")
internal fun <Self : Value<Self>> Value<Self>.eq(that: AnyValue): BooleanValue {
    return if (this::class == that::class) eq(that as Self) else error("${this::class.simpleName} cannot be compared to ${that::class.simpleName}")
}

public abstract class NumberValue<Self : NumberValue<Self>> : Value<Self>() {
    internal abstract infix fun add(that: Self): Self
    internal abstract infix fun sub(that: Self): Self
    internal abstract infix fun mul(that: Self): Self
    internal abstract infix fun div(that: Self): Self
    internal abstract infix fun mod(that: Self): Self
    internal abstract infix fun gt(that: Self): BooleanValue
    internal abstract infix fun lt(that: Self): BooleanValue
    internal abstract infix fun gteq(that: Self): BooleanValue
    internal abstract infix fun lteq(that: Self): BooleanValue
    internal abstract fun neg(): Self
}

public class IntegerValue(internal val value: Int) : NumberValue<IntegerValue>() {

    override val type: Type get() = IntegerType

    override infix fun add(that: IntegerValue): IntegerValue {
        return IntegerValue(this.value + that.value)
    }

    override infix fun sub(that: IntegerValue): IntegerValue {
        return IntegerValue(this.value - that.value)
    }

    override infix fun mul(that: IntegerValue): IntegerValue {
        return IntegerValue(this.value * that.value)
    }

    override infix fun div(that: IntegerValue): IntegerValue {
        return IntegerValue(this.value / that.value)
    }

    override infix fun mod(that: IntegerValue): IntegerValue {
        return IntegerValue(this.value % that.value)
    }

    override infix fun gt(that: IntegerValue): BooleanValue {
        return BooleanValue(this.value > that.value)
    }

    override infix fun lt(that: IntegerValue): BooleanValue {
        return BooleanValue(this.value < that.value)
    }

    override infix fun gteq(that: IntegerValue): BooleanValue {
        return BooleanValue(this.value >= that.value)
    }

    override infix fun lteq(that: IntegerValue): BooleanValue {
        return BooleanValue(this.value <= that.value)
    }

    override fun eq(that: IntegerValue): BooleanValue {
        return BooleanValue(this.value == that.value)
    }

    override fun neg(): IntegerValue {
        return IntegerValue(-this.value)
    }

    override fun stringify(): StringValue {
        return StringValue(value.toString())
    }
}

public class DecimalValue(internal val value: Float) : NumberValue<DecimalValue>() {

    override val type: Type get() = DecimalType

    override infix fun add(that: DecimalValue): DecimalValue {
        return DecimalValue(this.value + that.value)
    }

    override infix fun sub(that: DecimalValue): DecimalValue {
        return DecimalValue(this.value - that.value)
    }

    override infix fun mul(that: DecimalValue): DecimalValue {
        return DecimalValue(this.value * that.value)
    }

    override infix fun div(that: DecimalValue): DecimalValue {
        return DecimalValue(this.value / that.value)
    }

    override infix fun mod(that: DecimalValue): DecimalValue {
        return DecimalValue(this.value % that.value)
    }

    override infix fun gt(that: DecimalValue): BooleanValue {
        return BooleanValue(this.value > that.value)
    }

    override infix fun lt(that: DecimalValue): BooleanValue {
        return BooleanValue(this.value < that.value)
    }

    override infix fun gteq(that: DecimalValue): BooleanValue {
        return BooleanValue(this.value >= that.value)
    }

    override infix fun lteq(that: DecimalValue): BooleanValue {
        return BooleanValue(this.value <= that.value)
    }

    override fun eq(that: DecimalValue): BooleanValue {
        return BooleanValue(this.value == that.value)
    }

    override fun neg(): DecimalValue {
        return DecimalValue(this.value.unaryMinus())
    }

    override fun stringify(): StringValue {
        return StringValue(value.toString())
    }
}

public class BooleanValue(internal val value: Boolean) : Value<BooleanValue>() {

    override val type: Type get() = BooleanType

    internal fun not(): BooleanValue {
        return BooleanValue(this.value.not())
    }

    override fun eq(that: BooleanValue): BooleanValue {
        return BooleanValue(this.value == that.value)
    }

    override fun stringify(): StringValue = StringValue(value.toString())
}

public class StringValue(internal val value: String) : Value<StringValue>() {

    override val type: Type get() = StringType

    internal infix fun concat(that: StringValue): StringValue {
        return StringValue(this.value + that.value)
    }

    override fun eq(that: StringValue): BooleanValue {
        return BooleanValue(this.value == that.value)
    }

    override fun stringify(): StringValue = this
}

public class RecordValue(
    override val type: RecordType,
    private val values: MutableMap<String, AnyValue>,
) : Value<RecordValue>() {

    internal fun set(field: String, value: AnyValue) {
        requireNotNull(type.fields)
        values[field] = value
    }

    internal fun get(field: String): AnyValue {
        requireNotNull(type.fields)
        return values[field] ?: error("$field not found")
    }

    override fun eq(that: RecordValue): BooleanValue {
        return if (type == that.type) BooleanValue(values == that.values) else BooleanValue(false)
    }

    override fun stringify(): StringValue {
        val value = buildString {
            append(type.name)
            val fields = type.fields
            if (fields != null) {
                append(" { ")
                fields.forEachIndexed { index, field ->
                    if (index > 0) append(", ")
                    append(field.identifier)
                    append(": ")
                    append(values[field.identifier])
                }
                append(" }")
            }
        }
        return StringValue(value)
    }
}

public object UnitValue : Value<UnitValue>() {

    override val type: Type get() = UnitType

    override fun eq(that: UnitValue): BooleanValue {
        return BooleanValue(true)
    }

    override fun stringify(): StringValue {
        return StringValue("sash.Unit")
    }
}

internal typealias AnyCallable = Callable<*>

public sealed class Callable<Self : Callable<Self>> : Value<Self>() {
    internal abstract fun call(args: List<AnyValue>): AnyValue
}

public class NativeFunction(
    override val type: Type,
    private val block: (List<AnyValue>) -> AnyValue,
) : Callable<NativeFunction>() {

    override fun call(args: List<AnyValue>): AnyValue {
        return block(args)
    }

    override fun eq(that: NativeFunction): BooleanValue {
        return BooleanValue(this == that)
    }

    override fun stringify(): StringValue {
        return StringValue("<native-function>")
    }
}

public class SimpleFunction(
    private val closure: Environment,
    private val statements: List<Statement>,
    private val params: List<Parameter>,
    override val type: Type,
) : Callable<SimpleFunction>() {

    override fun call(args: List<AnyValue>): AnyValue {
        val env = Environment(closure)
        params.fastZipEach(args) { param, arg -> env.create(param.name, arg) }
        statements.fastForEach { evalStatement(env, it) }
        return UnitValue
    }

    override fun eq(that: SimpleFunction): BooleanValue {
        return BooleanValue(this == that)
    }

    override fun stringify(): StringValue {
        return StringValue("<function>")
    }
}
