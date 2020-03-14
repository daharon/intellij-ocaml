package org.ocaml.ide.mli

import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType

import org.ocaml.lang.parser.OcamlParserDefinition

class OcamlInterfaceParserDefinition : OcamlParserDefinition() {

    companion object {
        val FILE = IFileElementType(OcamlInterfaceLanguage)
    }

    override fun createFile(viewProvider: FileViewProvider): PsiFile? =
        OcamlInterfacePsiFileRoot(viewProvider)

    override fun getFileNodeType(): IFileElementType? = FILE
}