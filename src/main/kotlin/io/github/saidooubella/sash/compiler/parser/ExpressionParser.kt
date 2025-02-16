package io.github.saidooubella.sash.compiler.parser

import io.github.saidooubella.sash.compiler.input.MutableInput
import io.github.saidooubella.sash.compiler.input.consume
import io.github.saidooubella.sash.compiler.input.isNotDone
import io.github.saidooubella.sash.compiler.parser.context.ParserContext
import io.github.saidooubella.sash.compiler.parser.nodes.*
import io.github.saidooubella.sash.compiler.parser.utils.*
import io.github.saidooubella.sash.compiler.tokens.Token
import io.github.saidooubella.sash.compiler.tokens.TokenType
import io.github.saidooubella.sash.compiler.utils.DelimitedList
import io.github.saidooubella.sash.compiler.utils.buildDelimitedList

internal fun expression(context: ParserContext, input: MutableInput<Token>, basic: Boolean): RawExpression {
    return assignment(context, input, basic)
}

private fun assignment(context: ParserContext, input: MutableInput<Token>, basic: Boolean): RawExpression {
    val target = disjunction(context, input, basic)
    val assign = input.consumeTokenOrNull(context, TokenType.Equal) ?: return target
    return AssignmentRawExpression(target, assign, expression(context, input, basic))
}

private fun disjunction(context: ParserContext, input: MutableInput<Token>, basic: Boolean): RawExpression {
    var left = conjunction(context, input, basic)
    while (input.isNotDone && input.current.type == TokenType.PipePipe) {
        left = LogicalBinaryRawExpression(left, input.consume(), conjunction(context, input, basic))
    }
    return left
}

private fun conjunction(context: ParserContext, input: MutableInput<Token>, basic: Boolean): RawExpression {
    var left = equality(context, input, basic)
    while (input.isNotDone && input.current.type == TokenType.AmpersandAmpersand) {
        left = LogicalBinaryRawExpression(left, input.consume(), equality(context, input, basic))
    }
    return left
}

private fun equality(context: ParserContext, input: MutableInput<Token>, basic: Boolean): RawExpression {
    var left = comparison(context, input, basic)
    while (input.isNotDone && (input.current.type == TokenType.EqualEqual || input.current.type == TokenType.BangEqual)) {
        left = BinaryRawExpression(left, input.consume(), comparison(context, input, basic))
    }
    return left
}

private fun comparison(context: ParserContext, input: MutableInput<Token>, basic: Boolean): RawExpression {

    fun isComparisonOperator(): Boolean {
        return input.current.type == TokenType.GreaterThan
                || input.current.type == TokenType.LessThan
                || input.current.type == TokenType.GreaterThanEqual
                || input.current.type == TokenType.LessThanEqual
    }

    var left = additive(context, input, basic)

    while (input.isNotDone && isComparisonOperator()) {
        left = BinaryRawExpression(left, input.consume(), additive(context, input, basic))
    }

    return left
}

private fun additive(context: ParserContext, input: MutableInput<Token>, basic: Boolean): RawExpression {
    var left = multiplicative(context, input, basic)
    while (input.isNotDone && (input.current.type == TokenType.Plus || input.current.type == TokenType.Minus)) {
        left = BinaryRawExpression(left, input.consume(), multiplicative(context, input, basic))
    }
    return left
}

private fun multiplicative(context: ParserContext, input: MutableInput<Token>, basic: Boolean): RawExpression {

    fun isMultiplicativeOperator(): Boolean {
        return input.current.type == TokenType.Star ||
                input.current.type == TokenType.Slash ||
                input.current.type == TokenType.Percent
    }

    var left = prefix(context, input, basic)

    while (input.isNotDone && isMultiplicativeOperator()) {
        left = BinaryRawExpression(left, input.consume(), prefix(context, input, basic))
    }

    return left
}

private fun prefix(context: ParserContext, input: MutableInput<Token>, basic: Boolean): RawExpression {
    return when (input.current.type) {
        TokenType.Bang, TokenType.Plus, TokenType.Minus -> UnaryRawExpression(input.consume(), prefix(context, input, basic))
        else -> postfix(context, input, basic)
    }
}

private fun postfix(context: ParserContext, input: MutableInput<Token>, basic: Boolean): RawExpression {

    var left = primary(context, input, basic)

    while (input.isNotDone) {

        if (input.current.type == TokenType.Dot) {
            val dot = input.consume()
            val identifier = input.consumeToken(context, TokenType.Identifier, "field name")
            left = AccessRawExpression(left, dot, identifier)
            continue
        }

        val canHaveLambda = !basic || canHaveTrailingLambda(input)

        if (isCallable(input.current.type, canHaveLambda)) {

            val typeArgs = typeArgumentsOrNull(context, input)
            val valueArgs = valueArgumentsOrNull(context, input)

            if (canHaveLambda && input.current.type == TokenType.OpenBrace) {
                val trailingFn = TailFunctionRawArgs(valueArgs, functionRawExpression(context, input))
                return InvokeRawExpression(left, typeArgs, trailingFn)
            }

            left = InvokeRawExpression(left, typeArgs, valueArgs ?: valueArguments(context, input))

        } else {
            break
        }
    }

    return left
}

private fun isCallable(type: TokenType, canHaveLambda: Boolean): Boolean {
    return type == TokenType.OpenBracket || type == TokenType.OpenParent || (canHaveLambda && type == TokenType.OpenBrace)
}

private fun canHaveTrailingLambda(input: MutableInput<Token>): Boolean {

    tailrec fun peekPastOpenBrace(offset: Int = 0): Int {
        return when (input.peek(offset).type) {
            TokenType.EndOfFile, TokenType.SemiColon -> -1
            TokenType.OpenBrace -> offset + 1
            else -> peekPastOpenBrace(offset + 1)
        }
    }

    var offset = peekPastOpenBrace()
    if (offset == -1) return false

    var depth = 1

    while (depth > 0) {
        when (input.peek(offset).type) {
            TokenType.EndOfFile -> return false
            TokenType.OpenBrace -> depth++
            TokenType.CloseBrace -> depth--
            else -> Unit
        }
        offset++
    }

    return when (input.peek(offset).type) {
        TokenType.OpenBrace -> true
        else -> false
    }
}

private fun valueArgumentsOrNull(context: ParserContext, input: MutableInput<Token>): SimpleRawArgs? {
    return if (input.current.type == TokenType.OpenParent) valueArguments(context, input) else null
}

private fun valueArguments(context: ParserContext, input: MutableInput<Token>): SimpleRawArgs {
    val openParent = input.consumeToken(context, TokenType.OpenParent, "(")

    val args = buildDelimitedList {
        input.consumeWhile(context, { input.current.type != TokenType.CloseParent }) {
            addElement(expression(context, input, false))
            if (input.current.type != TokenType.CloseParent) {
                addDelimiter(input.consumeToken(context, TokenType.Comma, ","))
            }
        }
    }

    val closeParent = input.consumeToken(context, TokenType.CloseParent, ")")
    return SimpleRawArgs(openParent, args, closeParent)
}

private fun primary(context: ParserContext, input: MutableInput<Token>, basic: Boolean): RawExpression {
    return when (input.current.type) {
        TokenType.TrueKeyword, TokenType.FalseKeyword -> BooleanRawExpression(input.consume())
        TokenType.IntegerLiteral -> IntegerRawExpression(input.consume())
        TokenType.DecimalLiteral -> DecimalRawExpression(input.consume())
        TokenType.OpenParent -> parenthesisedRawExpression(context, input, basic)
        TokenType.IfKeyword -> ifRawExpression(context, input)
        TokenType.DoubleQuote -> stringRawExpression(context, input)
        TokenType.OpenBrace -> functionRawExpression(context, input)
        else -> identifierRawExpression(context, input)
    }
}

private fun ifRawExpression(context: ParserContext, input: MutableInput<Token>): IfRawExpression {
    val ifKeyword = input.consume()
    val condition = expression(context, input, true)
    val ifBody = controlBody(context, input)
    val elseIfChain = elseIfClauses(input, context)
    val elseClause = elseRawClause(input, context)
    return IfRawExpression(ifKeyword, condition, ifBody, elseIfChain, elseClause)
}

private fun elseIfClauses(input: MutableInput<Token>, context: ParserContext): List<ElseIfRawClause> {

    fun isElseIfClause(): Boolean {
        return input.current.type == TokenType.ElseKeyword && input.peek(1).type == TokenType.IfKeyword
    }

    return buildList {
        input.consumeWhile(context, ::isElseIfClause) {
            val elseKeyword = input.consume()
            val ifKeyword = input.consume()
            val condition = expression(context, input, true)
            val ifBody = controlBody(context, input)
            add(ElseIfRawClause(elseKeyword, ifKeyword, condition, ifBody))
        }
    }
}

private fun elseRawClause(input: MutableInput<Token>, context: ParserContext): ElseRawClause? {
    val elseKeyword = input.consumeTokenOrNull(context, TokenType.ElseKeyword) ?: return null
    return ElseRawClause(elseKeyword, controlBody(context, input))
}

private fun parenthesisedRawExpression(context: ParserContext, input: MutableInput<Token>, basic: Boolean): RawExpression {
    val openParent = input.consume()
    val expression = expression(context, input, basic)
    val closeParent = input.consumeToken(context, TokenType.CloseParent, ")")
    return ParenthesizedRawExpression(openParent, expression, closeParent)
}

private fun identifierRawExpression(context: ParserContext, input: MutableInput<Token>): IdentifierRawExpression {
    val identifier = input.consumeToken(context, TokenType.Identifier, "expression")
    return IdentifierRawExpression(identifier)
}

private fun functionRawExpression(context: ParserContext, input: MutableInput<Token>): FunctionRawExpression {
    val openBrace = input.consume()
    val params = functionParams(input, context)

    val statements = buildList {
        input.consumeWhile(context, { input.current.type != TokenType.CloseBrace }) {
            add(statement(context, input))
        }
    }

    val closeBrace = input.consumeToken(context, TokenType.CloseBrace, "}")
    return FunctionRawExpression(openBrace, params, statements, closeBrace)
}

private fun functionParams(input: MutableInput<Token>, context: ParserContext): FunctionRawParams? {
    input.mark(context)

    val paramsList = functionParamList(input, context)
    val arrow = input.consumeTokenOrNull(context, TokenType.Arrow)

    if (arrow == null) {
        input.rewind(context)
        return null
    }

    input.done(context)
    return FunctionRawParams(paramsList, arrow)
}

private fun functionParamList(input: MutableInput<Token>, context: ParserContext): DelimitedList<RawParameter, Token> {
    fun hasMoreParams(): Boolean {
        return input.current.type != TokenType.Arrow
                && input.current.type != TokenType.OpenBrace
                && input.current.type != TokenType.CloseBrace
    }

    fun parameter(): RawParameter {
        val identifier = input.consumeToken(context, TokenType.Identifier, "identifier")
        val type = typeAnnotationOrNull(context, input)
        return RawParameter(identifier, type)
    }

    return buildDelimitedList {
        if (input.current.type == TokenType.Arrow) return@buildDelimitedList input.current.run {
            context.reporter.reportUnexpectedToken(start, end, "a function parameter", null)
        }
        input.consumeWhile(context, { hasMoreParams() }) {
            addElement(parameter())
            if (hasMoreParams()) {
                addDelimiter(input.consumeToken(context, TokenType.Comma, ","))
            }
        }
    }
}

private fun stringRawExpression(context: ParserContext, input: MutableInput<Token>): StringRawExpression {
    val quote = input.consume()
    val value = input.consumeTokenOrNull(context, TokenType.StringLiteral)
    val end = value?.end ?: quote.end

    if (input.current.type == TokenType.DoubleQuote && end aligns input.current.start) {
        return StringRawExpression(quote, value, input.consume())
    }

    context.reporter.reportUnterminatedString(end, end)
    return StringRawExpression(quote, value, null)
}
