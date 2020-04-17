package org.ocaml.ide.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.components.service
import com.intellij.openapi.project.guessProjectForFile
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

import org.ocaml.ide.service.MerlinService
import org.ocaml.util.LineNumbering
import org.ocaml.util.ReversedSubstringCharSequence

class OcamlCompletionContributor : CompletionContributor() {

    init {

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(),
                object : CompletionProvider<CompletionParameters>() {
                    override fun addCompletions(parameters: CompletionParameters,
                                                context: ProcessingContext,
                                                resultSet: CompletionResultSet) {
                        val ln = LineNumbering(parameters.originalFile.text)
                        val merlinService = guessProjectForFile(parameters.originalFile.virtualFile)
                            ?.service<MerlinService>()
                            ?: return
                        val prefix = findSuitablePrefix(parameters).trim()
                        val completions = merlinService.completions(parameters.originalFile,
                                prefix, ln.position(parameters.offset))
                        for (completion in completions) {
                            if (prefix in completion.name) {
                                resultSet.addElement(
                                    LookupElementBuilder.create(completion.name).withTypeText(completion.desc)
                                )
                            }
                        }
                    }
                })
    }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }

    private fun findSuitablePrefix(parameters: CompletionParameters): String {
        val originalPosition = parameters.originalPosition?.textOffset ?: 0
        return findEmacsOcamlAtom(parameters.originalFile.text, originalPosition)
    }

    private fun findEmacsOcamlAtom(text: String, offset: Int): String {
        val re = Regex("[a-zA-Z0-9.']*[~?]?")
        val endIndex = re.find(ReversedSubstringCharSequence(text, offset, 0))?.next()?.range?.last

        return if (endIndex != null) {
            text.substring(offset - endIndex, offset + 1)
        } else {
            ""
        }
    }
}