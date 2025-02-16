package io.github.saidooubella.sash.compiler.parser.context

import io.github.saidooubella.sash.compiler.diagnostics.DiagnosticsReporter

public class ParserContext(
    internal val reporter: DiagnosticsReporter,
) {
    internal var canReportErrors: Boolean = true
}
