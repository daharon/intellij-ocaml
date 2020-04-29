package org.ocaml.ide.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

import org.ocaml.merlin.*

/**
 * Project-level service providing the Merlin editor service.
 */
class MerlinService(private val project: Project) : Disposable {

    private val merlin = Merlin(project)

    init {
        // Listen for changes so Merlin can stay current on the editor's state.
        EditorFactory.getInstance().eventMulticaster
            .addDocumentListener(MerlinServiceDocumentListener(project), this)
    }

    override fun dispose() = merlin.close()

    fun errors(file: PsiFile): List<MerlinError> {
        ReadAction.run<Exception> {
            merlin.tellSource(file.virtualFile.canonicalPath!!, file.text)
        }
        return merlin.errors(file.virtualFile.canonicalPath!!)
    }

    fun completionsPartial(file: PsiFile, prefix: String, position: Position): List<CompletionEntry> {
        return merlin.expandPrefix(file.virtualFile.canonicalPath!!, prefix, position).entries
    }

    fun completions(file: PsiFile, prefix: String, position: Position): List<CompletionEntry> {
        return merlin.completePrefix(file.virtualFile.canonicalPath!!, prefix, position).entries
    }

    fun locate(file: PsiFile, position: Position): LocateResponse {
        return merlin.locate(file.virtualFile.canonicalPath!!, position)
    }

    fun document(file: PsiFile, position: Position): String {
        return merlin.document(file.virtualFile.canonicalPath!!, position)
    }

    fun typeEnclosing(file: PsiFile, position: Position): List<TypeDefinition> =
        merlin.typeEnclosing(file.virtualFile.canonicalPath!!, position)

    fun typeExpression(file: PsiFile, text: String, position: Position): List<TypeDefinition> =
        merlin.typeExpression(file.virtualFile.canonicalPath!!, text, position)

    fun tellSource(file: PsiFile) =
        file.virtualFile?.canonicalPath?.let {
            merlin.tellSource(it, file.text)
        }

    fun outline(file: PsiFile): List<Outline> =
        merlin.outline(file.virtualFile.canonicalPath!!)
}