package org.ocaml.ide.highlighter

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.guessProjectForFile
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.NotNull

import org.ocaml.ide.runconfig.OcamlRunner
import org.ocaml.ide.service.MerlinService
import org.ocaml.merlin.MerlinError
import org.ocaml.util.LineNumbering

/**
 * Created by sidharthkuruvila on 26/05/16.
 */
class MerlinErrorHighlightingAnnotator : ExternalAnnotator<MerlinInfo, Results>() {

    companion object {
        private val LOG = Logger.getInstance(OcamlRunner::class.java)
        val merlinErrors = mapOf(
                "type" to HighlightSeverity.ERROR,
                "typer" to HighlightSeverity.ERROR,
                "parser" to HighlightSeverity.ERROR,
                "env" to HighlightSeverity.ERROR,
                "warning" to HighlightSeverity.WARNING,
                "unknown" to HighlightSeverity.INFORMATION
        )
    }

    //TODO Add some intelligence here to help decide whether the annotator should run
    override fun collectInformation(@NotNull file: PsiFile): MerlinInfo {
        val merlinService = guessProjectForFile(file.virtualFile)
            ?.service<MerlinService>()
        return MerlinInfo(file, file.text, merlinService)
    }

    override fun doAnnotate(merlinInfo: MerlinInfo): Results? {
        val ln = LineNumbering(merlinInfo.source)
        val errors = merlinInfo.merlinService?.errors(merlinInfo.file) ?: emptyList()
        return Results(errors, ln)
    }

    override fun apply(file: PsiFile, results: Results, holder: AnnotationHolder) {
        val ln = results.lineNumbering
        val (_, f) = results.errors.partition { it.start == null || it.end == null }
        for (error in f) {
            val range = TextRange(ln.index(error.start!!), ln.index(error.end!!))
            if (!merlinErrors.containsKey(error.type)) {
                LOG.error("Unmapped error %s".format(f))
            }
            val severity = merlinErrors[error.type] ?: HighlightSeverity.ERROR
            val message = error.message
            holder.newAnnotation(severity, message).range(range).create()
        }
    }
}

data class MerlinInfo(
        val file: PsiFile,
        val source: String,
        val merlinService: MerlinService?
)

data class Results(val errors: List<MerlinError>, val lineNumbering: LineNumbering)