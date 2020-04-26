package org.ocaml.jbuild.highlighter

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiElement

class JbuilderHighlightingAnnotator : Annotator {
    private val stanzas = listOf("library", "executable", "executables", "rule", "ocamllex", "ocamlyacc", "menhir", "install",
            "alias", "copy_files", "copy_files#", "jbuild_version", "include", "documentation")

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (stanzas.contains(element.text)) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .enforcedTextAttributes(TextAttributes.ERASE_MARKER)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).enforcedTextAttributes(
                EditorColorsManager.getInstance()
                    .globalScheme.getAttributes(JbuilderSyntaxHighlighter.STANZA)
            )
        }
    }
}