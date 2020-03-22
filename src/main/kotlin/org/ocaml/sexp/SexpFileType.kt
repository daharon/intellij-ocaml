package org.ocaml.sexp

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

import org.ocaml.lang.OcamlIcons

object SexpFileType : LanguageFileType(SexpLanguage) {

    override fun getDescription(): String = "Ocaml sexp"

    override fun getDefaultExtension(): String = "sexp"

    override fun getName(): String = "Sexp File"

    override fun getIcon(): Icon? = OcamlIcons.MODULE_ICON_FILE
}