package org.ocaml.ide.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

import org.ocaml.lang.parser.psi.*

private const val MAX_PLACEHOLDER_LENGTH = 20

/**
 * Code-folding for OCaml interface source files.
 */
class OcamlInterfaceFoldingBuilder : FoldingBuilderEx() {

    companion object {
        private val log = Logger.getInstance(OcamlInterfaceFoldingBuilder::class.java)
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> =
        PsiTreeUtil.collectElements(root) {
            it is ModuleExpr
                    || it is ModuleType
                    || it is TypeKind
        }.map {
            FoldingDescriptor(it, it.textRange)
        }.also {
            log.debug { "Folding regions:  ${it.map { it.range }}" }
        }.toTypedArray()

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false

    override fun getPlaceholderText(node: ASTNode): String? {
        return StringUtil.shortenTextWithEllipsis(node.text, MAX_PLACEHOLDER_LENGTH, 3)
    }
}