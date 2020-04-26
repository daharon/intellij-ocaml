package org.ocaml.ide.hints

import com.intellij.codeInsight.hints.HintInfo
import com.intellij.codeInsight.hints.InlayInfo
import com.intellij.codeInsight.hints.InlayParameterHintsProvider
import com.intellij.codeInsight.hints.Option
import com.intellij.codeInsight.hints.settings.ParameterNameHintsSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

import org.ocaml.ide.service.MerlinService
import org.ocaml.lang.lexer.OcamlTypes
import org.ocaml.lang.parser.OcamlPsiFileRoot
import org.ocaml.lang.parser.psi.*
import org.ocaml.merlin.Position

/**
 * Type hints inlaid within the editor.
 *
 * See [InlayParameterHintsProvider] documentation.
 */
@Suppress("UnstableApiUsage")
class OcamlInlayParameterHintsProvider : InlayParameterHintsProvider {

    private val paramNameHintSettings by lazy { ParameterNameHintsSettings.getInstance() }

    companion object {
        private const val TYPE_HINT_MAX_LENGTH = 40 // Characters
        private const val TYPE_HINT_SUFFIX_LENGTH = 15 // Characters
        // Option ID's.  Used to enable or disable parameter hints.
        private const val OPTION_ANONYMOUS_FUNCTION = "OCAML_PARAM_HINT_ANONYMOUS_FUNCTION"
        private const val OPTION_FUNCTION_EXPRESSION = "OCAML_PARAM_HINT_FUNCTION_EXPRESSION"
        private const val OPTION_LET_BINDING = "OCAML_PARAM_HINT_LET_BINDING"
    }

    override fun getParameterHints(element: PsiElement): MutableList<InlayInfo> {
        val merlin by lazy { element.project.service<MerlinService>() }
        val output = mutableListOf<InlayInfo>()
        fun addInlayInfo(text: String, element: PsiElement, outputList: MutableList<InlayInfo>) {
            val formattedText = StringUtil.shortenTextWithEllipsis(text, TYPE_HINT_MAX_LENGTH, TYPE_HINT_SUFFIX_LENGTH)
            val info = InlayInfo(
                formattedText, element.textRange.endOffset, relatesToPrecedingText = true,
                isShowOnlyIfExistedBefore = false, isFilterByBlacklist = true
            )
            outputList.add(info)
        }
        when {
            // Update Merlin when we start hinting a new file.
            element is OcamlPsiFileRoot ->
                merlin.tellSource(element)

            // Anonymous function parameters (fun ... -> ...).
            element is LabeledSimplePattern
                    && (element.context is NonOpExpr || element.context is FunDef)
                    && element.firstChild.firstChild !is SimplePatternNotIdent
                    // Do not type-hint for explicitly typed parameter.
                    && element.children.none { it is LabelLetPattern } ->
                getType(merlin, element)?.let { addInlayInfo(it, element, output) }

            // Type of function expression (function | ...).
            element.elementType == OcamlTypes.FUNCTION ->
                getType(merlin, element)?.let { addInlayInfo(it, element, output) }

            // Variable/function let-binding.
            element is ValDecl && element.context is LetBindingBody ->
                getType(merlin, element)?.let { addInlayInfo(it, element, output) }
        }
        return output
    }

    override fun getSupportedOptions(): MutableList<Option> {
        return mutableListOf(
            Option(
                id = "OCAML_PARAM_HINT_ANONYMOUS_FUNCTION",
                name = "Anonymous Functions",
                defaultValue = true
            ).apply {
                extendedDescription = "Provide type hints for anonymous function parameters (fun ... -> ...)"
            },
            Option(
                id = "OCAML_PARAM_HINT_FUNCTION_EXPRESSION",
                name = "Function Expressions",
                defaultValue = true
            ).apply {
                extendedDescription = "Provide type hints for function expressions (function | ...)"
            },
            Option(
                id = "OCAML_PARAM_HINT_LET_BINDING",
                name = "Let Bindings",
                defaultValue = true
            ).apply {
                extendedDescription = "Provide type hints for let-bindings (let a_func ... = ...)"
            }
        )
    }

    override fun getDefaultBlackList(): MutableSet<String> {
        return mutableSetOf()
    }

    override fun getHintInfo(element: PsiElement): HintInfo? {
        return null
    }

    override fun getInlayPresentation(inlayText: String): String = ": $inlayText"

    /**
     * Retrieve the type signature from Merlin for the PSI element provided.
     */
    private fun getType(merlin: MerlinService, element: PsiElement): String? {
        val position = Position.fromPsiElement(element)
        val definitions = merlin.typeEnclosing(element.containingFile, position)
        return definitions.firstOrNull()?.type
    }

    private fun getOption(id: String): Boolean =
        paramNameHintSettings.getOption(id) ?: false
}