package com.github.hx.rubocopcompletion

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.module.ModuleUtil
import com.intellij.util.ProcessingContext

/**
 * Offers completions to the editor in Ruby comments. When addCompletions is called, its parameters.position.text
 * will always be a comment, starting with a #. It offers three types of completions:
 *
 * 1. The rubocop: prefix
 * 2. The disable, enable, and to\do directives to immediately follow it.
 * 3. Names of cops, including "all"
 */
object CopsCompletionProvider : CompletionProvider<CompletionParameters>() {
    private const val cursor = "IntellijIdeaRulezzz" // Seriously. That's how JetBrains roll.

    private val directivePattern = Regex("""(#\s*\w*:?\w*)$cursor""")
    private val copNamePattern = Regex("""#\s*rubocop:(disable|enable|todo)\s+(?:[A-Za-z/]+,\s*)*?(al{0,2}|[A-Z][A-Za-z/]*)?$cursor""")

    private val handler = InsertHandler<LookupElement> { ctx, _ ->
        // So far this doesn't actually work :(
        AutoPopupController.getInstance(ctx.project).scheduleAutoPopup(ctx.editor, CompletionType.BASIC, null)
    }

    private val directiveElements = mapOf(
            "enable" to AllIcons.RunConfigurations.TestPassed,
            "disable" to AllIcons.RunConfigurations.TestFailed,
            "todo" to AllIcons.RunConfigurations.TestSkipped
    ).map { e ->
        LookupElementBuilder
                .create("# rubocop:${e.key} ")
                .withInsertHandler(handler)
                .withPresentableText("rubocop:${e.key}")
                .withIcon(e.value)
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, res: CompletionResultSet) {
        val text = parameters.position.text

        var match = directivePattern.find(text)
        if (match != null) {
            res.withPrefixMatcher(match.groupValues[1]).addAllElements(directiveElements)
            return
        }

        match = copNamePattern.find(text)
        if (match != null) {
            val module = ModuleUtil.findModuleForFile(parameters.originalFile) ?: return
            val schema = SchemaProvider.getSchemaForModule(module) ?: return

            res.stopHere()
            res.addLookupAdvertisement("Choose a cop to ${match.groupValues[1]}")
            res.caseInsensitive().withPrefixMatcher(match.groupValues[2]).addAllElements(schema.copNameCompletions)
        }
    }
}
