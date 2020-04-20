package org.ocaml.ide.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup.*
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
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
        // FIXME:  For some reason IntelliJ is not passing in the exact element.
        log.debug("Called ${this::class.simpleName}::getQuickNavigateInfo")
        log.debug("Element:  $element")
        log.debug("Original element:  $originalElement")
        val merlin = originalElement.project.service<MerlinService>()
        val position = Position.fromPsiElement(originalElement)
        val response = merlin.typeEnclosing(originalElement.containingFile, position)
        return response.joinToString {
            StringBuilder(DEFINITION_START).append(it.type).append(DEFINITION_END)
        }
        /*
        val response = merlin.typeExpression(originalElement.containingFile,
            originalElement.text, position)
        return response.joinToString {
            StringBuilder(DEFINITION_START).append(it.type).append(DEFINITION_END)
        }
        */
    }

    override fun generateDoc(element: PsiElement, originalElement: PsiElement?): String? {
        val response = StringBuilder()
        log.debug("Called ${this::class.simpleName}::generateDoc")

        val priElement = originalElement ?: element
        val merlin = priElement.project.service<MerlinService>()
        val position = Position.fromPsiElement(priElement)
        val definitions = merlin.typeEnclosing(priElement.containingFile, position)
        if (definitions.isNotEmpty()) {
            response.append(DEFINITION_START).append(definitions.first().type).append(DEFINITION_END)
        }

        val doc = this.generateHoverDoc(element, originalElement)
        log.debug("Document response:  $doc")
        response.append(CONTENT_START).append(formatContent(doc)).append(CONTENT_END)

        return response.toString()
    }

    override fun generateHoverDoc(element: PsiElement, originalElement: PsiElement?): String? {
        log.debug("Called ${this::class.simpleName}::generateHoverDoc")
        val priElement = originalElement ?: element
        val merlin = priElement.project.service<MerlinService>()
        val position = Position.fromPsiElement(priElement)
        return merlin.document(priElement.containingFile, position, priElement.text)
    }

    /**
     * Translate the documentation content from ODoc format to HTML.
     *
     * https://ocamlverse.github.io/content/documentation_guidelines.html#basic-documentation-syntax
     */
    private fun formatContent(content: String?): String? = content?.let {
        // Just replace new-lines with HTML <br>'s, for now.
        it.replace("\n", "<br>")
    }
}