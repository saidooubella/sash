package io.github.saidooubella.sash.compiler.refiner.symbols

import io.github.saidooubella.sash.compiler.utils.fastJoin
import io.github.saidooubella.sash.compiler.utils.fastZipEach

public sealed class Type {

    public fun assignableTo(type: Type): Boolean {
        return this == ErrorType || type == ErrorType || type is PlaceholderType || assignable(NothingType) || assignable(type)
    }

    protected abstract fun assignable(type: Type): Boolean
    public abstract fun stringify(): String
}

public class FunctionType internal constructor(
    public val typeParams: List<TypeParam>,
    public val valueParams: List<Type>,
    public val returnType: Type,
) : Type() {

    override fun assignable(type: Type): Boolean {

        if (type !is FunctionType || valueParams.size != type.valueParams.size) return false

        // Check parameter types (contravariant)
        valueParams.fastZipEach(type.valueParams) { thisParam, thatParam ->
            if (!thatParam.assignableTo(thisParam)) return false
        }

        // Check return type (covariant)
        return returnType.assignableTo(type.returnType)
    }

    override fun stringify(): String {
        val params = valueParams.fastJoin { it.stringify() }
        return "($params) -> ${returnType.stringify()}"
    }
}

public class PlaceholderType(internal val param: TypeParam) : Type() {
    override fun assignable(type: Type): Boolean = true
    override fun stringify(): String = "???"
}

public object ErrorType : Type() {
    override fun assignable(type: Type): Boolean = true
    override fun stringify(): String = "???"
}

public sealed class NamedType : Type() {
    public abstract val name: String
}

public class TypeParam(override val name: String) : NamedType() {
    override fun assignable(type: Type): Boolean = this == type
    override fun stringify(): String = name
}

public class RecordType internal constructor(
    public val typeParams: List<TypeParam>,
    override val name: String,
    public val concreteTypes: Map<TypeParam, Type> = emptyMap(),
    fields: List<Field>? = null,
) : NamedType() {

    public var fields: List<Field>? = fields
        internal set(value) {
            check(field == null)
            field = value
        }

    override fun assignable(type: Type): Boolean {
        return type is RecordType && name == type.name
    }

    override fun stringify(): String = buildString {
        append(name)
        if (typeParams.isNotEmpty()) {
            append("[")
            typeParams.forEachIndexed { index, param ->
                if (index > 0) append(", ")
                append(concreteTypes[param]?.stringify() ?: param.name)
            }
            append("]")
        }
    }

    public data class Field internal constructor(
        val identifier: String,
        val type: Type,
    )
}
