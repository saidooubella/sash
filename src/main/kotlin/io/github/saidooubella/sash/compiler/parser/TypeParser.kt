package io.github.saidooubella.sash.compiler.parser

import io.github.saidooubella.sash.compiler.input.MutableInput
import io.github.saidooubella.sash.compiler.parser.context.ParserContext
import io.github.saidooubella.sash.compiler.parser.nodes.*
import io.github.saidooubella.sash.compiler.parser.utils.consumeToken
import io.github.saidooubella.sash.compiler.parser.utils.consumeTokenOrNull
import io.github.saidooubella.sash.compiler.parser.utils.consumeWhile
import io.github.saidooubella.sash.compiler.tokens.Token
import io.github.saidooubella.sash.compiler.tokens.TokenType
import io.github.saidooubella.sash.compiler.utils.DelimitedList
import io.github.saidooubella.sash.compiler.utils.buildDelimitedList

internal fun typeArgumentsOrNull(context: ParserContext, input: MutableInput<Token>): TypeRawArgs? {
    val openBracket = input.consumeTokenOrNull(context, TokenType.OpenBracket) ?: return null

    val args = buildDelimitedList {
        if (input.current.type == TokenType.CloseBracket) return@buildDelimitedList input.current.run {
            context.reporter.reportUnexpectedToken(start, end, "a type", null)
        }
        input.consumeWhile(context, { input.current.type != TokenType.CloseBracket }) {
            addElement(type(context, input))
            if (input.current.type != TokenType.CloseBracket) {
                addDelimiter(input.consumeToken(context, TokenType.Comma, ","))
            }
        }
    }

    val closeBracket = input.consumeToken(context, TokenType.CloseBracket, "]")
    return TypeRawArgs(openBracket, args, closeBracket)
}

internal fun typeAnnotationOrNull(context: ParserContext, input: MutableInput<Token>): RawTypeAnnotation? {
    return if (input.current.type == TokenType.Colon) typeAnnotation(context, input) else null
}

internal fun typeAnnotation(context: ParserContext, input: MutableInput<Token>): RawTypeAnnotation {
    val colon = input.consumeToken(context, TokenType.Colon, ":")
    return RawTypeAnnotation(colon, type = type(context, input))
}

internal fun type(context: ParserContext, input: MutableInput<Token>): RawType {
    return when (input.current.type) {
        TokenType.OpenParent -> functionType(context, input)
        else -> simpleType(context, input)
    }
}

private fun functionType(context: ParserContext, input: MutableInput<Token>): RawFunctionType {
    val openParent = input.consumeToken(context, TokenType.OpenParent, "(")
    val params = valueParameters(context, input)
    val closeParent = input.consumeToken(context, TokenType.CloseParent, ")")
    val arrow = input.consumeToken(context, TokenType.Arrow, "->")
    val returnType = type(context, input)
    return RawFunctionType(openParent, params, closeParent, arrow, returnType)
}

private fun valueParameters(context: ParserContext, input: MutableInput<Token>): DelimitedList<RawType, Token> {
    return buildDelimitedList {
        input.consumeWhile(context, { input.current.type != TokenType.CloseParent }) {
            addElement(type(context, input))
            if (input.current.type != TokenType.CloseParent) {
                addDelimiter(input.consumeToken(context, TokenType.Comma, ","))
            }
        }
    }
}

private fun simpleType(context: ParserContext, input: MutableInput<Token>): RawSimpleType {
    val name = input.consumeToken(context, TokenType.Identifier, "type")
    val typeArgs = typeArgumentsOrNull(context, input)
    return RawSimpleType(name, typeArgs)
}
