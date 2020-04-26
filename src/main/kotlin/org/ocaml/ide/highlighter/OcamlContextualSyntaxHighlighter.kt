package org.ocaml.ide.highlighter

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

import org.ocaml.lang.lexer.OcamlTypes
import org.ocaml.lang.parser.psi.*

/**
 * Additional syntax highlighting which cannot be performed without the PSI element tree.
 */
class OcamlContextualSyntaxHighlighter : Annotator {

    companion object {
        private fun setAttr(holder: AnnotationHolder, key: TextAttributesKey, range: TextRange) =
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(range)
                .textAttributes(key)
                .create()
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) =
        when {
            // Type parameters
            element is SimpleCoreType2 && element.firstChild.elementType == OcamlTypes.QUOTE ->
                setAttr(holder, OcamlSyntaxHighlighter.TYPE_PARAMETER, element.textRange)
            element is OptionalTypeParameter ->
                setAttr(holder, OcamlSyntaxHighlighter.TYPE_PARAMETER, element.textRange)

            // Core data types (int, float, etc.)
            element is TypeLongident && element.firstChild.elementType == OcamlTypes.LIDENT ->
                setAttr(holder, OcamlSyntaxHighlighter.CORE_TYPE, element.textRange)

            // Module types (List.t, Option.t, etc)
            element.elementType == OcamlTypes.LIDENT && element.context is TypeLongident ->
                setAttr(holder, OcamlSyntaxHighlighter.DECLARED_TYPE, element.textRange)
            element.elementType == OcamlTypes.LIDENT && element.context is TypeDeclaration ->
                setAttr(holder, OcamlSyntaxHighlighter.DECLARED_TYPE, element.textRange)

            // Function declaration (let a_func x y = ...)
            element is ValDecl && element.context is LetBindingBody ->
                setAttr(holder, OcamlSyntaxHighlighter.DECLARED_FUNCTION, element.textRange)

            // Module
            element is ModLongident && element.parent !is ConstrIdent && element.parent !is ConstrLongident ->
                setAttr(holder, OcamlSyntaxHighlighter.UIDENTIFIER, element.textRange)

            // Name tag
            element is NameTag ->
                setAttr(holder, OcamlSyntaxHighlighter.NAME_TAG, element.textRange)

            // Constructors/ADTs
            element is ConstrIdent || element is ConstrLongident ->
                setAttr(holder, OcamlSyntaxHighlighter.CONSTRUCTOR, element.textRange)

            // Labeled function parameters
            element is LabelVar || element is LabelIdent ->
                setAttr(holder, OcamlSyntaxHighlighter.LABELED_PARAMETER, element.textRange)

            // Optional function parameters
            element.elementType == OcamlTypes.LABEL_OP -> {
                val textRange = TextRange(element.textRange.startOffset + 1, element.textRange.endOffset - 1)
                setAttr(holder, OcamlSyntaxHighlighter.LABELED_PARAMETER, textRange)
            }

            else -> { /* Pass */ }
        }
}