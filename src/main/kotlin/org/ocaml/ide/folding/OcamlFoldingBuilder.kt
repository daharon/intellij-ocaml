package org.ocaml.ide.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement

import org.ocaml.lang.parser.psi.*

private const val MAX_PLACEHOLDER_LENGTH = 20

/**
 * Code-folding for OCaml source.
 */
class OcamlFoldingBuilder : FoldingBuilderEx() {

    companion object {
        private val log = Logger.getInstance(OcamlFoldingBuilder::class.java)
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        fun buildFoldRegionsInner(root: PsiElement, regions: Array<FoldingDescriptor>): Array<FoldingDescriptor> {
            // TODO: Add folding groups?
            log.debug { "Folding element:  ${root::class.simpleName}"}
            var newRegions = when (root) {
                is NonOpExpr,
                is FunBinding,
                is ModuleExpr,
                is ModuleType,
                is TypeKind -> {
                    if (StringUtil.containsLineBreak(root.text)) {
                        val region = FoldingDescriptor(root, root.textRange)
                        regions.plus(region)
                    } else {
                        regions
                    }
                }
                else -> regions
            }
            if (root.children.isNotEmpty()) {
                newRegions = buildFoldRegionsInner(root.firstChild, newRegions)
            }
            if (root.nextSibling != null) {
                newRegions = buildFoldRegionsInner(root.nextSibling, newRegions)
            }
            return newRegions
        }
        val regions = buildFoldRegionsInner(root, emptyArray())
        log.debug { "Folding regions:  ${regions.map { it.range }}" }
        return regions
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return StringUtil.shortenTextWithEllipsis(node.text, MAX_PLACEHOLDER_LENGTH, 3)
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}