package org.ocaml.sexp.parser

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

import org.ocaml.sexp.SexpFileType
import org.ocaml.sexp.SexpLanguage

class SexpPsiFileRoot(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, SexpLanguage) {
    override fun getFileType(): FileType = SexpFileType
}