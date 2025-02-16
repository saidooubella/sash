package io.github.saidooubella.sash.compiler.tokens

import io.github.saidooubella.sash.compiler.diagnostics.DiagnosticsReporter
import io.github.saidooubella.sash.compiler.span.PositionBuilder

internal sealed interface TokenizerMode {
    object Normal : TokenizerMode
    object String : TokenizerMode
}

public class TokenizerContext(
    internal val positionBuilder: PositionBuilder,
    internal val reporter: DiagnosticsReporter,
) {
    internal val builder: StringBuilder = StringBuilder()

    private val modes = ArrayDeque<TokenizerMode>()

    internal val currentMode: TokenizerMode
        get() = modes.lastOrNull() ?: TokenizerMode.Normal

    internal fun enterMode(mode: TokenizerMode) {
        modes.addLast(mode)
    }

    internal fun exitMode() {
        modes.removeLast()
    }
}
