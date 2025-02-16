package io.github.saidooubella.sash.compiler.refiner

import io.github.saidooubella.sash.compiler.parser.nodes.RawProgram
import io.github.saidooubella.sash.compiler.refiner.context.RefinerContext
import io.github.saidooubella.sash.compiler.refiner.nodes.Program
import io.github.saidooubella.sash.compiler.refiner.symbols.UnitType
import io.github.saidooubella.sash.compiler.refiner.symbols.withBuiltins
import io.github.saidooubella.sash.compiler.utils.fastMap

@Suppress("FunctionName")
public fun Refine(program: RawProgram, context: RefinerContext): Program = context.withBuiltins {
    val statements = program.statements.fastMap { refineStatement(context, it) }
    checkReturnPaths(context, statements, UnitType)
    Program(statements)
}
