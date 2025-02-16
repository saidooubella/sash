package io.github.saidooubella.sash.compiler.parser

import io.github.saidooubella.sash.compiler.input.MutableInput
import io.github.saidooubella.sash.compiler.parser.context.ParserContext
import io.github.saidooubella.sash.compiler.parser.nodes.RawControlBody
import io.github.saidooubella.sash.compiler.parser.utils.consumeToken
import io.github.saidooubella.sash.compiler.parser.utils.consumeWhile
import io.github.saidooubella.sash.compiler.tokens.Token
import io.github.saidooubella.sash.compiler.tokens.TokenType

internal fun controlBody(context: ParserContext, input: MutableInput<Token>): RawControlBody {

    val openBrace = input.consumeToken(context, TokenType.OpenBrace, "{")

    val statements = buildList {
        input.consumeWhile(context, { input.current.type != TokenType.CloseBrace }) {
            add(statement(context, input))
        }
    }

    val closeBrace = input.consumeToken(context, TokenType.CloseBrace, "}")
    return RawControlBody(openBrace, statements, closeBrace)
}
