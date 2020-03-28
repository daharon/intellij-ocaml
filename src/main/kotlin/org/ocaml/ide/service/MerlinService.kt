package org.ocaml.ide.service

import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile

import org.ocaml.merlin.*

class MerlinService {

    val merlin by lazy { MerlinProtocol2.newInstance() }

    fun errors(file: PsiFile): List<MerlinError> {
        reloadFileIfModified(file)
        MerlinProtocol3().errors(file.virtualFile)
        return merlin.errors(file.virtualFile.canonicalPath!!)
    }

    fun completions(file: PsiFile, prefix: String, position: Position): List<CompletionEntry> {
        reloadFileIfModified(file)
        MerlinProtocol3().completePrefix(file.virtualFile, prefix, position)
        return merlin.complete(file.virtualFile.canonicalPath!!, prefix, position).entries
    }

    fun locate(file: PsiFile, position: Position): LocateResponse {
        reloadFileIfModified(file)
        MerlinProtocol3().locate(file.virtualFile, position)
        return merlin.locate(file.virtualFile.canonicalPath!!, position)
    }

    private fun reloadFileIfModified(file: PsiFile) {
        val doc = PsiDocumentManager.getInstance(file.project).getCachedDocument(file)
        val filename = file.virtualFile.canonicalPath!!
        if (doc == null || doc.getUserData(MerlinServiceDocumentListener.DOCUMENT_CHANGED) != false) {
            //merlin.seekExact(filename, Position(1, 0))
            //merlin.drop(filename)
            merlin.tellSource(filename, file.text)
            doc?.putUserData(MerlinServiceDocumentListener.DOCUMENT_CHANGED, false)
        }
    }
}