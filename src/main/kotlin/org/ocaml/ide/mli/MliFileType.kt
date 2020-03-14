package org.ocaml.ide.mli

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

import org.ocaml.lang.OcamlIcons

/**
 * Created by sidharthkuruvila on 17/05/16.
 */
object MliFileType : LanguageFileType(OcamlInterfaceLanguage) {

    override fun getDescription(): String = "Ocaml interface file"

    override fun getDefaultExtension(): String = "mli"

    override fun getName(): String = "Ocaml interface file"

    override fun getIcon(): Icon? = OcamlIcons.INTERFACE_ICON_FILE
}