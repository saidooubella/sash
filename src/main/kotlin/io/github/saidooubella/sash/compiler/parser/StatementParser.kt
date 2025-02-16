package io.github.saidooubella.sash.compiler.parser

import io.github.saidooubella.sash.compiler.input.MutableInput
import io.github.saidooubella.sash.compiler.input.consume
import io.github.saidooubella.sash.compiler.parser.context.ParserContext
import io.github.saidooubella.sash.compiler.parser.nodes.*
import io.github.saidooubella.sash.compiler.parser.utils.consumeToken
import io.github.saidooubella.sash.compiler.parser.utils.consumeTokenOrNull
import io.github.saidooubella.sash.compiler.parser.utils.consumeWhile
import io.github.saidooubella.sash.compiler.tokens.Token
import io.github.saidooubella.sash.compiler.tokens.TokenType
import io.github.saidooubella.sash.compiler.utils.DelimitedList
import io.github.saidooubella.sash.compiler.utils.buildDelimitedList
import io.github.saidooubella.sash.compiler.utils.emptyDelimitedList

internal fun statement(context: ParserContext, input: MutableInput<Token>): RawStatement {
    return when (input.current.type) {
        TokenType.ContinueKeyword -> continueStatement(context, input)
        TokenType.DefKeyword -> definitionStatement(context, input)
        TokenType.ReturnKeyword -> returnStatement(context, input)
        TokenType.BreakKeyword -> breakStatement(context, input)
        TokenType.WhileKeyword -> whileStatement(context, input)
        TokenType.SemiColon -> emptyStatement(context, input)
        TokenType.Underscore -> dropStatement(context, input)
        else -> expressionStatement(context, input)
    }
}

private fun dropStatement(context: ParserContext, input: MutableInput<Token>): RawStatement {
    val underscore = input.consume()
    val equal = input.consumeToken(context, TokenType.Equal, "=")
    val expression = expression(context, input, false)
    val semi = input.consumeToken(context, TokenType.SemiColon, ";")
    return DropStatement(underscore, equal, expression, semi)
}

private fun emptyStatement(context: ParserContext, input: MutableInput<Token>): RawStatement {
    val semis = buildList {
        input.consumeWhile(context, { input.current.type == TokenType.SemiColon }) {
            add(input.consume())
        }
    }
    val start = semis.first().start
    val end = semis.last().end
    context.reporter.reportRedundantSemicolon(start, end, semis.size)
    return EmptyRawStatement(semis, start, end)
}

private fun whileStatement(context: ParserContext, input: MutableInput<Token>): RawStatement {
    val whileKeyword = input.consume()
    val condition = expression(context, input, true)
    val body = controlBody(context, input)
    return WhileRawStatement(whileKeyword, condition, body)
}

private fun returnStatement(context: ParserContext, input: MutableInput<Token>): RawStatement {
    val returnKeyword = input.consume()
    val expression = if (input.current.type != TokenType.SemiColon) expression(context, input, false) else null
    val semi = input.consumeToken(context, TokenType.SemiColon, ";")
    return ReturnRawStatement(returnKeyword, expression, semi)
}

private fun definitionStatement(context: ParserContext, input: MutableInput<Token>): RawStatement {
    val defKeyword = input.consume()
    val mutKeyword = input.consumeTokenOrNull(context, TokenType.MutKeyword)
    val identifier = input.consumeToken(context, TokenType.Identifier, "identifier")
    val typeParams = typeParametersOrNull(context, input)
    val type = typeAnnotationOrNull(context, input)
    val equal = input.consumeToken(context, TokenType.Equal, "=")
    val initializer = definitionInitializer(context, input)
    val semi = input.consumeToken(context, TokenType.SemiColon, ";")
    return DefinitionRawStatement(defKeyword, mutKeyword, identifier, typeParams, type, equal, initializer, semi)
}

private fun definitionInitializer(context: ParserContext, input: MutableInput<Token>): RawInitializer {
    return when (input.current.type) {
        TokenType.RecordKeyword -> recordInitializer(input, context)
        TokenType.EnumKeyword -> enumInitializer(input, context)
        else -> expressionInitializer(context, input)
    }
}

private fun recordInitializer(input: MutableInput<Token>, context: ParserContext): RawInitializer {
    val recordKeyword = input.consume()
    return RecordRawInitializer(recordKeyword, fieldsOrNull(input, context))
}

private fun expressionInitializer(context: ParserContext, input: MutableInput<Token>): ExpressionRawInitializer {
    return ExpressionRawInitializer(expression(context, input, false))
}

private fun enumInitializer(input: MutableInput<Token>, context: ParserContext): EnumRawInitializer {
    val enumKeyword = input.consume()
    val openBrace = input.consumeToken(context, TokenType.OpenBrace, "{")
    val entries = enumEntries(input, context)
    val closeBrace = input.consumeToken(context, TokenType.CloseBrace, "}")
    return EnumRawInitializer(enumKeyword, openBrace, entries, closeBrace)
}

private fun enumEntries(input: MutableInput<Token>, context: ParserContext): DelimitedList<EnumRawEntry, Token> {
    if (input.current.type == TokenType.OpenBrace) {
        val (_, _, start, end) = input.current
        context.reporter.reportUnexpectedToken(start, end, "an enum entry", null)
        return emptyDelimitedList()
    }

    return buildDelimitedList {
        input.consumeWhile(context, { input.current.type != TokenType.CloseBrace }) {
            addElement(enumEntry(context, input))
            if (input.current.type != TokenType.CloseBrace) {
                addDelimiter(input.consumeToken(context, TokenType.Comma, ","))
            }
        }
    }
}

private fun enumEntry(context: ParserContext, input: MutableInput<Token>): EnumRawEntry {
    val identifier = input.consumeToken(context, TokenType.Identifier, "an identifier")
    return EnumRawEntry(identifier, fieldsOrNull(input, context))
}

private fun fieldsOrNull(input: MutableInput<Token>, context: ParserContext): RawFields? {
    val openBrace = input.consumeTokenOrNull(context, TokenType.OpenBrace) ?: return null
    val fields = buildDelimitedList {
        if (input.current.type == TokenType.OpenBrace) return@buildDelimitedList input.current.run<Token, Unit> {
            context.reporter.reportUnexpectedToken(start, end, "a field", null)
        }
        input.consumeWhile(context, { input.current.type != TokenType.CloseBrace }) {
            this.addElement(field(context, input))
            if (input.current.type != TokenType.CloseBrace) {
                this.addDelimiter(input.consumeToken(context, TokenType.Comma, ","))
            }
        }
    }
    val closeBrace = input.consumeToken(context, TokenType.CloseBrace, "}")
    return RawFields(openBrace, fields, closeBrace)
}

private fun field(context: ParserContext, input: MutableInput<Token>): RawField {
    val identifier = input.consumeToken(context, TokenType.Identifier, "an identifier")
    val colon = input.consumeToken(context, TokenType.Colon, ":")
    return RawField(identifier, colon, type(context, input))
}

private fun typeParametersOrNull(context: ParserContext, input: MutableInput<Token>): RawTypeParameters? {
    if (input.current.type != TokenType.OpenBracket) return null
    val openBracket = input.consume()
    val typeParams = buildDelimitedList {
        if (input.current.type == TokenType.CloseBracket) return@buildDelimitedList input.current.run {
            context.reporter.reportUnexpectedToken(start, end, "an identifier", null)
        }
        input.consumeWhile(context, { input.current.type != TokenType.CloseBracket }) {
            addElement(input.consumeToken(context, TokenType.Identifier, "type name"))
            if (input.current.type != TokenType.CloseBracket) {
                addDelimiter(input.consumeToken(context, TokenType.Comma, ","))
            }
        }
    }
    val closeBracket = input.consumeToken(context, TokenType.CloseBracket, "]")
    return RawTypeParameters(openBracket, typeParams, closeBracket)
}

private fun expressionStatement(context: ParserContext, input: MutableInput<Token>): RawStatement {

    val expression = expression(context, input, false)

    if (input.current.type == TokenType.CloseBrace) {
        return ImplicitResultRawStatement(expression)
    }

    val semi = when (expression) {
        is IfRawExpression -> input.consumeTokenOrNull(context, TokenType.SemiColon)
        else -> input.consumeToken(context, TokenType.SemiColon, ";")
    }

    return ExpressionRawStatement(expression, semi)
}

private fun breakStatement(context: ParserContext, input: MutableInput<Token>): RawStatement {
    val breakKeyword = input.consume()
    val semi = input.consumeToken(context, TokenType.SemiColon, ";")
    return BreakRawStatement(breakKeyword, semi)
}

private fun continueStatement(context: ParserContext, input: MutableInput<Token>): RawStatement {
    val continueKeyword = input.consume()
    val semi = input.consumeToken(context, TokenType.SemiColon, ";")
    return ContinueRawStatement(continueKeyword, semi)
}
