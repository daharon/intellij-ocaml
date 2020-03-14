package org.ocaml.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * Created by sidharthkuruvila on 27/04/16.
 */
object MlFileType : LanguageFileType(OcamlLanguage) {

    override fun getDescription(): String = "Ocaml source file"

    override fun getDefaultExtension(): String = "ml"

    override fun getName(): String = "Ocaml file"

    override fun getIcon(): Icon? = OcamlIcons.MODULE_ICON_FILE
}