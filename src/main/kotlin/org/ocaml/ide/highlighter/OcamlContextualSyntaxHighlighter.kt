package org.ocaml.ide.highlighter

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

import org.ocaml.lang.lexer.OcamlTypes
import org.ocaml.lang.parser.psi.*

/**
 * Additional syntax highlighting which cannot be performed without the PSI element tree.
 */
class OcamlContextualSyntaxHighlighter : Annotator {

    companion object {
        private fun setAttr(element: PsiElement, holder: AnnotationHolder, key: TextAttributesKey) {
            holder.createInfoAnnotation(element, null)
                .textAttributes = key
        }
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) =
        when {
            // Type parameters
            element is SimpleCoreType2 && element.firstChild.elementType == OcamlTypes.QUOTE ->
                setAttr(element, holder, OcamlSyntaxHighlighter.TYPE_PARAMETER)
            element is OptionalTypeParameter ->
                setAttr(element, holder, OcamlSyntaxHighlighter.TYPE_PARAMETER)

            // Core data types (int, float, etc.)
            element is TypeLongident /* SimpleCoreType2 */ && element.firstChild.elementType == OcamlTypes.LIDENT ->
                setAttr(element, holder, OcamlSyntaxHighlighter.CORE_TYPE)

            // Module types (List.t, Option.t, etc)
            element.elementType == OcamlTypes.LIDENT && element.context is TypeLongident ->
                setAttr(element, holder, OcamlSyntaxHighlighter.DECLARED_TYPE)
            element.elementType == OcamlTypes.LIDENT && element.context is TypeDeclaration ->
                setAttr(element, holder, OcamlSyntaxHighlighter.DECLARED_TYPE)

            // Function declaration (let a_func x y = ...)
            element is ValDecl && element.context is LetBindingBody ->
                setAttr(element, holder, OcamlSyntaxHighlighter.DECLARED_FUNCTION)

            // Module
            element is ModLongident && element.parent !is ConstrIdent && element.parent !is ConstrLongident ->
                setAttr(element, holder, OcamlSyntaxHighlighter.UIDENTIFIER)

            // Name tag
            element is NameTag ->
                setAttr(element, holder, OcamlSyntaxHighlighter.NAME_TAG)

            // Constructors/ADTs
            element is ConstrIdent || element is ConstrLongident ->
                setAttr(element, holder, OcamlSyntaxHighlighter.CONSTRUCTOR)

            else -> { /* Pass */ }
        }
}