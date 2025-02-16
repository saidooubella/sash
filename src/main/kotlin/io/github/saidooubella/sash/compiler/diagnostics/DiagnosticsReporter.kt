package io.github.saidooubella.sash.compiler.diagnostics

import io.github.saidooubella.sash.compiler.refiner.symbols.Type
import io.github.saidooubella.sash.compiler.refiner.symbols.TypeParam
import io.github.saidooubella.sash.compiler.span.Position

public class DiagnosticsReporter(private val fileName: String) {

    private val reports = mutableListOf<Diagnostic>()

    private val drafts = ArrayDeque<MutableList<Diagnostic>>()

    private fun report(start: Position, end: Position, message: String) {
        val storage = drafts.lastOrNull() ?: reports
        storage += Diagnostic(fileName, message, start, end)
    }

    internal fun draft() {
        drafts.addLast(ArrayList())
    }

    internal fun retain() {
        val draft = removeDraft()
        val storage = drafts.lastOrNull() ?: reports
        storage += draft
    }

    internal fun discard() {
        removeDraft()
    }

    private fun removeDraft(): List<Diagnostic> {
        return drafts.removeLastOrNull() ?: error("`collect()` must be called first")
    }

    public fun build(): List<Diagnostic> = reports.toList()

    internal fun reportIllegalCharacter(start: Position, end: Position, text: String) {
        report(start, end, "Invalid character `$text`")
    }

    internal fun reportUnexpectedToken(start: Position, end: Position, expected: String, actual: String?) {
        val message = when (actual) {
            null -> "Expected $expected"
            else -> "Expected `$expected` but got `$actual`"
        }
        report(start, end, message)
    }

    internal fun reportNumberOverflow(start: Position, end: Position, number: String, type: Type) {
        report(start, end, "Cannot fit `$number` inside `${type.stringify()}`")
    }

    internal fun reportInvalidBinaryOperation(
        start: Position,
        end: Position,
        operator: String,
        left: Type,
        right: Type,
    ) {
        report(start, end, "Cannot apply `$operator` on `${left.stringify()}` and `${right.stringify()}`")
    }

    internal fun reportInvalidUnaryOperation(start: Position, end: Position, operator: String, operand: Type) {
        report(start, end, "Cannot apply `$operator` on `${operand.stringify()}`")
    }

    internal fun reportDuplicateSymbol(start: Position, end: Position, identifier: String) {
        report(start, end, "`$identifier` has already been used")
    }

    internal fun reportUndefinedSymbol(start: Position, end: Position, identifier: String) {
        report(start, end, "`$identifier` is not defined")
    }

    internal fun reportNotFunctionTarget(start: Position, end: Position) {
        report(start, end, "Cannot call a non function typed value")
    }

    internal fun reportAssignmentTypeMismatch(start: Position, end: Position, actual: Type, target: Type) {
        report(
            start,
            end,
            "A value of type `${actual.stringify()}` cannot be assigned to a binding of type `${target.stringify()}`"
        )
    }

    internal fun reportArgumentTypeMismatch(start: Position, end: Position, actual: Type, target: Type) {
        report(
            start,
            end,
            "A value of type `${actual.stringify()}` cannot be passed as an argument to a parameter of type `${target.stringify()}`"
        )
    }

    internal fun reportUnmatchedArgsCount(start: Position, end: Position, expected: Int, actual: Int) {
        report(start, end, "Unexpected arguments count: expected `$expected`, but got `$actual`")
    }

    internal fun reportUnmatchedTypeArgsCount(start: Position, end: Position, expected: Int, actual: Int) {
        report(start, end, "Unexpected type arguments count: expected `$expected`, but got `$actual`")
    }

    internal fun reportInvalidReturnUsage(start: Position, end: Position) {
        report(start, end, "The `return` statement can only be used within the boundary of a function")
    }

    internal fun reportMissingReturnValue(start: Position, end: Position, returnType: Type) {
        report(start, end, "A return value of type `${returnType.stringify()}` is missing")
    }

    internal fun reportInvalidAssignmentTarget(start: Position, end: Position) {
        report(start, end, "Cannot assign values to this symbol")
    }

    internal fun reportRequiredReturnValue(start: Position, end: Position) {
        report(start, end, "A `return` statement is required at the end of this function")
    }

    internal fun reportUnreachableCode(start: Position, end: Position) {
        report(start, end, "Unreachable code")
    }

    internal fun reportInvalidContinueUsage(start: Position, end: Position) {
        report(start, end, "The `continue` statement can only be used inside a loop")
    }

    internal fun reportInvalidBreakUsage(start: Position, end: Position) {
        report(start, end, "The `break` statement can only be used inside a loop")
    }

    internal fun reportNonFunctionParametrizedDefinition(start: Position, end: Position) {
        report(start, end, "Only function and type binding can have type parameters")
    }

    internal fun reportInvalidReturnType(start: Position, end: Position, actual: Type, expected: Type) {
        report(
            start,
            end,
            "Cannot return a value of type `${actual.stringify()}` by a function that returns `${expected.stringify()}`"
        )
    }

    internal fun reportCannotInferType(start: Position, end: Position, identifier: String) {
        report(start, end, "Cannot infer the type of `$identifier`. Please specify it explicitly")
    }

    internal fun reportUnspecifiedTypes(start: Position, end: Position, unspecified: Set<TypeParam>) {
        val message = when (unspecified.size) {
            0 -> error("`unspecified` cannot be of size zero")
            1 -> "Cannot infer the type parameter `${unspecified.first().stringify()}`"
            else -> "Cannot infer these type parameters [ ${unspecified.joinToString { it.stringify() }} ]"
        }
        report(start, end, message)
    }

    internal fun reportUnterminatedString(start: Position, end: Position) {
        report(start, end, "Unterminated string")
    }

    internal fun reportUnclosedComment(start: Position, end: Position) {
        report(start, end, "Unclosed comment")
    }

    internal fun reportInvalidEscaping(start: Position, end: Position, esc: String) {
        report(start, end, "Invalid escaping \\$esc")
    }

    internal fun reportIncompleteEscaping(start: Position, end: Position) {
        report(start, end, "Expected and escaping sequence")
    }

    internal fun reportRedundantSemicolon(start: Position, end: Position, count: Int) {
        report(start, end, "Redundant " + if (count > 1) "semicolons" else "semicolon")
    }

    internal fun reportInvalidConditionType(start: Position, end: Position) {
        report(start, end, "The condition type must be of type `Boolean`")
    }

    internal fun reportInvalidImplicitResultUsage(start: Position, end: Position) {
        report(start, end, "Implicit results are allowed only directly within `functions` and `if` expressions")
    }

    internal fun reportIfMissingElse(start: Position, end: Position) {
        report(start, end, "`if` must have an `else` branches when used as an expression")
    }

    internal fun reportInvalidYieldType(start: Position, end: Position, actual: Type, expected: Type) {
        val message = "`${actual.stringify()}` cannot be yielded by a block that requires `${expected.stringify()}`"
        report(start, end, message)
    }

    internal fun reportMissingResultValue(start: Position, end: Position, expected: Type) {
        report(start, end, "Expected a value of type `${expected.stringify()}`")
    }

    internal fun reportUnitDiscard(start: Position, end: Position) {
        report(start, end, "`Unit` values cannot be discarded")
    }

    internal fun reportUnusedExpression(start: Position, end: Position) {
        report(start, end, "This expression's result must be discarded explicitly with ` _ = <expression> `")
    }

    internal fun reportMutableParametrizedDefinition(start: Position, end: Position) {
        report(start, end, "Definitions with type parameters cannot be mutable")
    }

    internal fun reportReadonlyAssignment(start: Position, end: Position) {
        report(start, end, "You cannot reassign a readonly definition. Consider adding the `mut` keyword")
    }

    internal fun reportStandaloneGenericType(start: Position, end: Position, identifier: String) {
        report(start, end, "Cannot use generic definition `$identifier` without instantiation")
    }

    internal fun reportMutableRecordDefinition(start: Position, end: Position) {
        report(start, end, "Record definitions cannot be mutable. Consider removing the 'mut' keyword")
    }

    internal fun reportParametrizedUnitRecord(start: Position, end: Position) {
        report(start, end, "Unit records cannot have type parameters")
    }
}
