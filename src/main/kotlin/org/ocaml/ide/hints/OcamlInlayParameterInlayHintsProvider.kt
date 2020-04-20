package org.ocaml.ide.hints

import com.intellij.codeInsight.hints.HintInfo
import com.intellij.codeInsight.hints.InlayInfo
import com.intellij.codeInsight.hints.InlayParameterHintsProvider
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

import org.ocaml.ide.service.MerlinService
import org.ocaml.lang.lexer.OcamlTypes
import org.ocaml.lang.parser.psi.*
import org.ocaml.merlin.Position

/**
 * Type hints inlaid within the editor.
 *
 * See [InlayParameterHintsProvider] documentation.
 */
@Suppress("UnstableApiUsage")
class OcamlInlayParameterInlayHintsProvider : InlayParameterHintsProvider {

    override fun getParameterHints(element: PsiElement?): MutableList<InlayInfo> {
        val output = mutableListOf<InlayInfo>()
        return when (element) {
            null -> output
            else -> {
                val merlin by lazy { element.project.service<MerlinService>() }
                when {
                    // Anonymous function parameters (fun ... -> ...).
                    element is LabeledSimplePattern
                            && (element.context is NonOpExpr || element.context is FunDef)
                            && element.firstChild.firstChild !is SimplePatternNotIdent
                            // Do not type-hint for explicitly typed parameter.
                            && element.children.none { it is LabelLetPattern } -> {
                        val type = getType(merlin, element)
                        val info = InlayInfo(
                            type, element.textRange.endOffset, relatesToPrecedingText = true,
                            isShowOnlyIfExistedBefore = false, isFilterByBlacklist = true
                        )
                        output.apply { this.add(info) }
                    }
                    // Type of function expression (function | ...).
                    element.elementType == OcamlTypes.FUNCTION -> {
                        val type = getType(merlin, element)
                        val info = InlayInfo(
                            type, element.textRange.endOffset, relatesToPrecedingText = true,
                            isShowOnlyIfExistedBefore = false, isFilterByBlacklist = true
                        )
                        output.apply { this.add(info) }
                    }
                    // Variable/function let-binding.
                    element is ValDecl && element.context is LetBindingBody -> {
                        val type = getType(merlin, element)
                        val info = InlayInfo(
                            type, element.textRange.endOffset, relatesToPrecedingText = true,
                            isShowOnlyIfExistedBefore = false, isFilterByBlacklist = true
                        )
                        output.apply { this.add(info) }
                    }
                    else -> output
                }
            }
        }
    }

    override fun getDefaultBlackList(): MutableSet<String> {
        return mutableSetOf()
    }

    override fun getHintInfo(element: PsiElement?): HintInfo? {
        return null
    }

    override fun getInlayPresentation(inlayText: String): String = ": $inlayText"

    /**
     * Retrieve the type signature from Merlin for the PSI element provided.
     */
    private fun getType(merlin: MerlinService, element: PsiElement): String {
        val position = Position.fromPsiElement(element)
        val definitions = merlin.typeEnclosing(element.containingFile, position)
        return if (definitions.isNotEmpty()) {
            definitions.first().type
        } else {
            "Type not found"
        }
    }
}