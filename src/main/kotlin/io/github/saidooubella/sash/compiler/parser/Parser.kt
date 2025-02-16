package io.github.saidooubella.sash.compiler.parser

import io.github.saidooubella.sash.compiler.input.MutableInput
import io.github.saidooubella.sash.compiler.parser.context.ParserContext
import io.github.saidooubella.sash.compiler.parser.nodes.RawProgram
import io.github.saidooubella.sash.compiler.parser.utils.consumeWhile
import io.github.saidooubella.sash.compiler.tokens.Token

@Suppress("FunctionName")
public fun Parse(input: MutableInput<Token>, context: ParserContext): RawProgram {
    val statements = buildList {
        input.consumeWhile(context) { add(statement(context, input)) }
    }
    return RawProgram(statements, endOfFile = input.current)
}
