package org.ocaml.ide.service

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

/**
 * Listen to document changes and update the state of Merlin.
 */
class MerlinServiceDocumentListener(private val project: Project) : DocumentListener {

    private val merlin by lazy { project.service<MerlinService>() }

    override fun documentChanged(event: DocumentEvent) {
        PsiDocumentManager.getInstance(project).getCachedPsiFile(event.document)?.run {
            merlin.tellSource(this)
        }
    }

    override fun beforeDocumentChange(event: DocumentEvent) =
        documentChanged(event)
}