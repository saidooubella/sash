package io.github.saidooubella.sash.evaluator

import io.github.saidooubella.sash.compiler.refiner.nodes.Program
import io.github.saidooubella.sash.compiler.utils.fastForEach

@Suppress("FunctionName")
public fun Evaluator(program: Program, env: Environment = builtInEnvironment()) {
    program.statements.fastForEach { evalStatement(env, it) }
}
