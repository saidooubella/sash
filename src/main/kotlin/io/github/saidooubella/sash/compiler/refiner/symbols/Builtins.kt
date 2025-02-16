package io.github.saidooubella.sash.compiler.refiner.symbols

import io.github.saidooubella.sash.compiler.refiner.context.RefinerContext

public val UnitType: RecordType = RecordType(listOf(), "Unit")

public val NothingType: RecordType = RecordType(listOf(), "Nothing")

public val BooleanType: RecordType = RecordType(listOf(), "Boolean")

public val StringType: RecordType = RecordType(listOf(), "String")

public val DecimalType: RecordType = RecordType(listOf(), "Float")

public val IntegerType: RecordType = RecordType(listOf(), "Int")

public val PrintLn: Definition = run {
    val typeParamT = TypeParam("T")
    Definition("println", FunctionType(listOf(typeParamT), listOf(typeParamT), UnitType), true)
}

public val Exit: Definition =
    Definition("exit", FunctionType(listOf(), listOf(IntegerType), NothingType), true)

public val TypeName: Definition = run {
    val typeParamT = TypeParam("T")
    Definition("type_name", FunctionType(listOf(typeParamT), listOf(typeParamT), StringType), true)
}

internal inline fun <T> RefinerContext.withBuiltins(block: () -> T): T {
    putType(NothingType)
    putType(UnitType)
    putType(StringType)
    putType(DecimalType)
    putType(IntegerType)
    putType(BooleanType)
    putDefinition(TypeName)
    putDefinition(PrintLn)
    putDefinition(Exit)
    return scoped(block)
}
