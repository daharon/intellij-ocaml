package org.ocaml.jbuild

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

import org.ocaml.lang.OcamlIcons

object JbuilderFileType : LanguageFileType(JbuilderLanguage, true) {

    override fun getDescription(): String = "Jbuilder config"

    override fun getDefaultExtension(): String = "jbuild"

    override fun getName(): String = "Jbuilder File"

    override fun getIcon(): Icon? = OcamlIcons.MODULE_ICON_FILE
}