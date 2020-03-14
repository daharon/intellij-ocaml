package org.ocaml.ide.mli

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class OcamlInterfacePsiFileRoot(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, OcamlInterfaceLanguage) {
    override fun getFileType(): FileType = MliFileType
}
