package com.github.hx.rubocopcompletion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns

/**
 * This is IntelliJ's entrypoint into the plugin for autocompletion in comments. It registers
 * the CopsCompletionProvider singleton as a completion contributor for Ruby comments.
 */
class CopsCompletion : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiComment(), CopsCompletionProvider)
    }
}
