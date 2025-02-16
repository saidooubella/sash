package io.github.saidooubella.sash.compiler.refiner

import io.github.saidooubella.sash.compiler.refiner.nodes.BinaryOperator
import io.github.saidooubella.sash.compiler.refiner.nodes.LogicalBinaryOperator
import io.github.saidooubella.sash.compiler.refiner.nodes.UnaryOperator
import io.github.saidooubella.sash.compiler.refiner.symbols.*
import io.github.saidooubella.sash.compiler.tokens.Token
import io.github.saidooubella.sash.compiler.tokens.TokenType

internal fun refineLogicalBinaryOperator(operator: Token): LogicalBinaryOperator {
    return when (operator.type) {
        TokenType.AmpersandAmpersand -> LogicalBinaryOperator.Conjunction
        TokenType.PipePipe -> LogicalBinaryOperator.Disjunction
        else -> error("Unhandled logical binary operator: `${operator.text}`")
    }
}

internal fun refineBinaryOperator(operator: Token): BinaryOperator {
    return when (operator.type) {
        TokenType.Plus -> BinaryOperator.Addition
        TokenType.Minus -> BinaryOperator.Subtraction
        TokenType.Star -> BinaryOperator.Multiplication
        TokenType.Slash -> BinaryOperator.Division
        TokenType.GreaterThan -> BinaryOperator.GreaterThan
        TokenType.LessThan -> BinaryOperator.LessThan
        TokenType.GreaterThanEqual -> BinaryOperator.GreaterThanOrEqual
        TokenType.LessThanEqual -> BinaryOperator.LessThanOrEqual
        TokenType.EqualEqual -> BinaryOperator.Equal
        TokenType.BangEqual -> BinaryOperator.NotEqual
        TokenType.Percent -> BinaryOperator.Modulo
        else -> error("Unhandled binary operator: `${operator.text}`")
    }
}

internal fun refineUnaryOperator(operator: Token): UnaryOperator {
    return when (operator.type) {
        TokenType.Bang -> UnaryOperator.LogicalNegation
        TokenType.Minus -> UnaryOperator.Negation
        TokenType.Plus -> UnaryOperator.Identity
        else -> error("Unhandled unary operator: `${operator.text}`")
    }
}

internal fun resolveBinaryOperationType(left: Type, operator: BinaryOperator, right: Type): Type? {
    return when (operator) {

        BinaryOperator.Equal, BinaryOperator.NotEqual -> if (left == right) BooleanType else null

        BinaryOperator.GreaterThan, BinaryOperator.GreaterThanOrEqual, BinaryOperator.LessThan, BinaryOperator.LessThanOrEqual -> when {
            matchType(left, right, IntegerType) -> BooleanType
            matchType(left, right, DecimalType) -> BooleanType
            else -> null
        }

        BinaryOperator.Subtraction, BinaryOperator.Multiplication, BinaryOperator.Division, BinaryOperator.Modulo -> when {
            matchType(left, right, IntegerType) -> IntegerType
            matchType(left, right, DecimalType) -> DecimalType
            else -> null
        }

        BinaryOperator.Addition -> when {
            matchType(left, right, IntegerType) -> IntegerType
            matchType(left, right, DecimalType) -> DecimalType
            matchType(left, right, StringType) -> StringType
            else -> null
        }
    }
}

internal fun resolveUnaryOperationType(operator: UnaryOperator, operand: Type): Type? {
    return when (operator) {

        UnaryOperator.Identity, UnaryOperator.Negation -> when (operand) {
            IntegerType -> IntegerType
            DecimalType -> DecimalType
            else -> null
        }

        UnaryOperator.LogicalNegation -> when (operand) {
            BooleanType -> BooleanType
            else -> null
        }
    }
}

internal fun resolveLogicalBinaryOperationType(left: Type, right: Type): Type? {
    return when (matchType(left, right, BooleanType)) {
        true -> BooleanType
        else -> null
    }
}

private fun matchType(left: Type, right: Type, actual: Type): Boolean = left == actual && right == actual
