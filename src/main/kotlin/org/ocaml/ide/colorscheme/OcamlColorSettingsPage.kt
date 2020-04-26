package org.ocaml.ide.colorscheme

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage

import org.ocaml.ide.highlighter.OcamlSyntaxHighlighter
import org.ocaml.lang.OcamlIcons

import javax.swing.Icon

/**
 * Created by sidharthkuruvila on 04/05/16.
 */
class OcamlColorSettingsPage : ColorSettingsPage {

    companion object {
        val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Brace", OcamlSyntaxHighlighter.BRACE),
            AttributesDescriptor("Bracket", OcamlSyntaxHighlighter.BRACKET),
            AttributesDescriptor("Paren", OcamlSyntaxHighlighter.PAREN),
            AttributesDescriptor("Number", OcamlSyntaxHighlighter.NUMBER),
            AttributesDescriptor("Uppercase Identifier", OcamlSyntaxHighlighter.UIDENTIFIER),
            AttributesDescriptor("Lowercase Identifier", OcamlSyntaxHighlighter.IDENTIFIER),
            AttributesDescriptor("Keyword", OcamlSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("String", OcamlSyntaxHighlighter.STRING),
            AttributesDescriptor("Comment", OcamlSyntaxHighlighter.COMMENT),
            AttributesDescriptor("Documentation Comment", OcamlSyntaxHighlighter.DOC_COMMENT),
            AttributesDescriptor("Operator", OcamlSyntaxHighlighter.OPERATOR),

            AttributesDescriptor("Core Type", OcamlSyntaxHighlighter.CORE_TYPE),
            AttributesDescriptor("Declared Type", OcamlSyntaxHighlighter.DECLARED_TYPE),
            AttributesDescriptor("Declared Function", OcamlSyntaxHighlighter.DECLARED_FUNCTION),
            AttributesDescriptor("Type Parameter", OcamlSyntaxHighlighter.TYPE_PARAMETER),
            AttributesDescriptor("Variant", OcamlSyntaxHighlighter.CONSTRUCTOR),
            AttributesDescriptor("Polymorphic Variant", OcamlSyntaxHighlighter.NAME_TAG),
            AttributesDescriptor("Labeled Function Parameter", OcamlSyntaxHighlighter.LABELED_PARAMETER)
        )
    }

    override fun getIcon(): Icon? = OcamlIcons.MODULE_ICON_FILE

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey>? = null

    override fun getHighlighter(): SyntaxHighlighter = OcamlSyntaxHighlighter()

    override fun getDemoText(): String = """
        open Str
        
        (* Example comment *)
        let x = 4 + 3
        let y = [ "one"; "two" ]
        let z = [| 1; 3;|]
        let a = (4, 5)
        Printf.printf "Number:  %d\n" 10;
        print_endline "String:  %s\n" "example";
        
        (** Example documentation comment *)
        module type Module_type = sig
            type t
            val make : 'a -> 'a list -> t
        end
        
        module Module_impl = struct
            type 'a t = {
                x : 'a;
                y : 'a list;
            }
            let make x y = { ~x; ~y }
        end
        
        module Make (M : Module_type) : Module_impl with type t = M.t = struct
            include M
        end""".trimIndent()

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Ocaml"
}