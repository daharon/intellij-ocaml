package org.ocaml.ide.service

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.BulkAwareDocumentListener
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager

/**
 * Listen to document changes and update the state of Merlin.
 */
class MerlinServiceDocumentListener(private val project: Project) : BulkAwareDocumentListener.Simple {

    private val merlin by lazy { project.service<MerlinService>() }

    override fun bulkUpdateFinished(document: Document) = updateMerlin(document)

    override fun afterDocumentChange(document: Document) = updateMerlin(document)

    private fun updateMerlin(document: Document) {
        PsiDocumentManager.getInstance(project).getPsiFile(document)?.run {
            merlin.tellSource(this)
        }
    }
}