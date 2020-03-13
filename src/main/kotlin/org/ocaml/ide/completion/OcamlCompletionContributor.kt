package org.ocaml.ide.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import org.ocaml.ide.components.MerlinServiceComponent
import org.ocaml.util.LineNumbering
import org.ocaml.util.ReversedSubstringCharSequence

class OcamlCompletionContributor : CompletionContributor() {

    val merlinService = ApplicationManager.getApplication().getComponent(MerlinServiceComponent::class.java)!!

    init {

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(),
                object : CompletionProvider<CompletionParameters>() {
                    override fun addCompletions(parameters: CompletionParameters,
                                                context: ProcessingContext,
                                                resultSet: CompletionResultSet) {
                        val ln = LineNumbering(parameters.originalFile.text)
                        val completions = merlinService.completions(parameters.originalFile,
                                findSuitablePrefix(parameters), ln.position(parameters.offset))
                        for (completion in completions) {
                            resultSet.addElement(LookupElementBuilder.create(completion.name).withTypeText(completion.desc))
                        }
                    }
                })
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