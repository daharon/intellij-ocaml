package org.ocaml.ide.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.components.service
import com.intellij.openapi.project.guessProjectForFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.util.ProcessingContext

import org.ocaml.ide.service.MerlinService
import org.ocaml.merlin.CompletionEntry
import org.ocaml.merlin.Position

/**
 * OCaml code-completion extension.
 */
class OcamlCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), OcamlCompletionProvider())
    }

    /**
     * Provides possible code-completions given the provided PSI Element.
     *
     * Uses [MerlinService]'s `expand prefix` functionality.
     */
    class OcamlCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val merlinService = guessProjectForFile(parameters.originalFile.virtualFile)
                ?.service<MerlinService>()
                ?: return
            val position = parameters.originalPosition?.let { Position.fromPsiElement(it) }
                ?: return
            val prefix = findPrefix(parameters.position)
            insertCompletions(prefix, result)
            val completions = merlinService.completionsPartial(parameters.originalFile,
                prefix, position)
            for (completion in completions) {
                if (!dropCompletion(completion)) {
                    val completionName = completion.name.split('.', ignoreCase = true).last()
                    val lookupElement = LookupElementBuilder.create(completionName)
                        .withTypeText(completion.desc, true)
                    result.addElement(lookupElement)
                }
            }
        }

        /**
         * Manually add completions for annoying cases.
         */
        private fun insertCompletions(prefix: String, result: CompletionResultSet) {
            when (prefix) {
                in "in" -> result.addElement(LookupElementBuilder.create("in").withBoldness(true))
            }
        }

        /**
         * Generate prefix string for completions by looking back at previous
         * elements in the PSI tree.
         */
        private fun findPrefix(element: PsiElement): String {
            tailrec fun findPrefixInner(element: PsiElement, tail: String): String =
                when (element.prevSibling) {
                    is PsiWhiteSpace -> tail
                    null -> findPrefixInner(element.parent, tail)
                    else -> findPrefixInner(element.prevSibling, element.prevSibling.text + tail)
                }
            val initial = element.text.replaceFirst("IntellijIdeaRulezzz", "", false)
            return findPrefixInner(element, initial)
        }

        private fun dropCompletion(entry: CompletionEntry): Boolean =
            entry.name.contains("__", ignoreCase = true)
    }
}