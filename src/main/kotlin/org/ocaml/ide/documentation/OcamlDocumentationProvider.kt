package org.ocaml.ide.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement

import org.ocaml.ide.service.MerlinService
import org.ocaml.merlin.Position


/**
 * Provide content for the documentation pop-up on `ctrl`-hover and the `View -> Quick Documentation` action.
 */
class OcamlDocumentationProvider : AbstractDocumentationProvider() {

    companion object {
        private val log = Logger.getInstance(OcamlDocumentationProvider::class.java)
    }

    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement): String? {
        log.info("Called ${this::class.simpleName}::getQuickNavigateInfo")
        val merlin = originalElement.project.service<MerlinService>()
        val position = psiElementToPosition(originalElement)
        val response = merlin.typeEnclosing(originalElement.containingFile, position)
        log.info("Type Enclosing response:  $response")
        return response
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        log.info("Called ${this::class.simpleName}::generateDoc")
        val response = this.generateHoverDoc(element, originalElement)
        log.info("Document response:  $response")
        return response
    }

    override fun generateHoverDoc(element: PsiElement, originalElement: PsiElement?): String? {
        log.info("Called ${this::class.simpleName}::generateHoverDoc")
        return if (originalElement != null) {
            val merlin = originalElement.project.service<MerlinService>()
            val position = psiElementToPosition(originalElement)
            merlin.document(originalElement.containingFile, position, originalElement.text)
        } else {
            val merlin = element.project.service<MerlinService>()
            val position = psiElementToPosition(element)
            merlin.document(element.containingFile, position, element.text)
        }
    }

    private fun psiElementToPosition(element: PsiElement): Position {
        val lineColumn = StringUtil.offsetToLineColumn(element.containingFile.text, element.textOffset)
        return Position(
            line = lineColumn.line + 1,
            col = lineColumn.column + 1)
    }
}