package org.ocaml.ide.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

import org.ocaml.merlin.*

class MerlinService(project: Project) : Disposable {

    val merlin by lazy { Merlin.newInstance(project) }

    init {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(MerlinServiceDocumentListener(project), this)
    }

    fun errors(file: PsiFile): List<MerlinError> {
        merlin.tellSource(file.virtualFile.canonicalPath!!, file.text)
        return merlin.errors(file.virtualFile.canonicalPath!!)
    }

    fun completions(file: PsiFile, prefix: String, position: Position): List<CompletionEntry> {
        return merlin.complete(file.virtualFile.canonicalPath!!, prefix, position).entries
    }

    fun locate(file: PsiFile, position: Position): LocateResponse {
        return merlin.locate(file.virtualFile.canonicalPath!!, position)
    }

    fun document(file: PsiFile, position: Position, identifier: String? = null): String {
        return merlin.document(file.virtualFile.canonicalPath!!, position, identifier)
    }

    fun typeEnclosing(file: PsiFile, position: Position): String {
        return merlin.typeEnclosing(file.virtualFile.canonicalPath!!, position)
    }

    fun tellSource(file: PsiFile) =
        file.virtualFile?.canonicalPath?.let {
            merlin.tellSource(it, file.text)
        }

    override fun dispose() {
        // TODO: Stop the Merlin process.
    }
}