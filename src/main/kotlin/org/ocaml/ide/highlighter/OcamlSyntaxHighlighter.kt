package org.ocaml.ide.highlighter

import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

import org.ocaml.lang.lexer.OcamlTypes
import org.ocaml.lang.lexer._OcamlLexer

/**
 * Lexer-based syntax highlighting.
 */
class OcamlSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        val BRACKET_TOKENS = setOf(
            OcamlTypes.LBRACKETBAR, OcamlTypes.LBRACKETAT, OcamlTypes.LBRACKETLESS, OcamlTypes.LBRACKETATATAT,
            OcamlTypes.LBRACKETPERCENT, OcamlTypes.GREATERRBRACKET, OcamlTypes.LBRACKET,
            OcamlTypes.LBRACKETPERCENTPERCENT, OcamlTypes.BARRBRACKET, OcamlTypes.LBRACKETATAT, OcamlTypes.RBRACKET,
            OcamlTypes.LBRACKETGREATER
        )

        val KEYWORD_TOKENS = setOf(
            OcamlTypes.DO,
            OcamlTypes.EXCEPTION,
            OcamlTypes.THEN,
            OcamlTypes.TYPE,
            OcamlTypes.OBJECT,
            OcamlTypes.MODULE,
            OcamlTypes.ASSERT,
            OcamlTypes.WHILE,
            OcamlTypes.DONE,
            OcamlTypes.NEW,
            OcamlTypes.FUN,
            OcamlTypes.MUTABLE,
            OcamlTypes.OPEN,
            OcamlTypes.WITH,
            OcamlTypes.BEGIN,
            OcamlTypes.VAL,
            OcamlTypes.CONSTRAINT,
            OcamlTypes.TRUE,
            OcamlTypes.WHEN,
            OcamlTypes.PRIVATE,
            OcamlTypes.TO,
            OcamlTypes.VIRTUAL,
            OcamlTypes.CLASS,
            OcamlTypes.FUNCTION,
            OcamlTypes.LAZY,
            OcamlTypes.END,
            OcamlTypes.FOR,
            OcamlTypes.METHOD_KEYWORD,
            OcamlTypes.INHERIT,
            OcamlTypes.ELSE,
            OcamlTypes.TRY,
            OcamlTypes.AS,
            OcamlTypes.EXTERNAL,
            OcamlTypes.IN,
            OcamlTypes.INCLUDE,
            OcamlTypes.MATCH,
            OcamlTypes.IF,
            OcamlTypes.AND,
            OcamlTypes.FALSE,
            OcamlTypes.STRUCT,
            OcamlTypes.OF,
            OcamlTypes.FUNCTOR,
            OcamlTypes.INITIALIZER,
            OcamlTypes.DOWNTO,
            OcamlTypes.LET,
            OcamlTypes.SIG,
            OcamlTypes.REC,
            OcamlTypes.OR
        )

        val BRACE_TOKENS = setOf(OcamlTypes.GREATERRBRACE, OcamlTypes.LBRACE, OcamlTypes.LBRACELESS, OcamlTypes.RBRACE)

        val NUMBER_TOKENS = setOf(
            OcamlTypes.INT32, OcamlTypes.NATIVEINT, OcamlTypes.INT, OcamlTypes.FLOAT,
            OcamlTypes.CHAR, OcamlTypes.INT64
        )

        val OPERATOR_TOKENS = setOf(
            OcamlTypes.COLONGREATER, OcamlTypes.STAR, OcamlTypes.LESS, OcamlTypes.PLUSDOT,
            OcamlTypes.EQUAL, OcamlTypes.BARBAR, OcamlTypes.BAR, OcamlTypes.MINUS, OcamlTypes.DOTDOT,
            OcamlTypes.SEMI, OcamlTypes.QUOTE, OcamlTypes.NONREC, OcamlTypes.QUESTION, OcamlTypes.AMPERAMPER,
            OcamlTypes.PLUS, OcamlTypes.TILDE, OcamlTypes.SHARP, OcamlTypes.PREFIXOP,
            OcamlTypes.MINUSGREATER, OcamlTypes.COLONEQUAL, OcamlTypes.PERCENT, OcamlTypes.COLONCOLON,
            OcamlTypes.INFIXOP4, OcamlTypes.BANG, OcamlTypes.INFIXOP1, OcamlTypes.INFIXOP0, OcamlTypes.INFIXOP3,
            OcamlTypes.INFIXOP2, OcamlTypes.LESSMINUS, OcamlTypes.MINUSDOT, OcamlTypes.GREATER, OcamlTypes.SHARPOP,
            OcamlTypes.PLUSEQ, OcamlTypes.AMPERSAND
        )

        val BRACKET =
            createTextAttributesKey("OCAML_BRACKET", DefaultLanguageHighlighterColors.BRACKETS)
        val BRACE =
            createTextAttributesKey("OCAML_BRACE", DefaultLanguageHighlighterColors.BRACES)
        val PAREN =
            createTextAttributesKey("OCAML_PAREN", DefaultLanguageHighlighterColors.PARENTHESES)
        val NUMBER =
            createTextAttributesKey("OCAML_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val UIDENTIFIER =
            createTextAttributesKey("OCAML_UIDENT", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        val IDENTIFIER =
            createTextAttributesKey("OCAML_IDENTIFIER", DefaultLanguageHighlighterColors.LOCAL_VARIABLE)
        val KEYWORD =
            createTextAttributesKey("OCAML_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val STRING =
            createTextAttributesKey("OCAML_STRING", DefaultLanguageHighlighterColors.STRING)
        val COMMENT =
            createTextAttributesKey("OCAML_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val DOC_COMMENT =
            createTextAttributesKey("OCAML_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
        val OPERATOR =
            createTextAttributesKey("OCAML_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        // Custom color scheme:
        val CORE_TYPE =
            createTextAttributesKey("OCAML_CORE_TYPE")
        val DECLARED_TYPE =
            createTextAttributesKey("OCAML_DECLARED_TYPE")
        val DECLARED_FUNCTION =
            createTextAttributesKey("OCAML_DECLARED_FUNCTION")
        val TYPE_PARAMETER =
            createTextAttributesKey("OCAML_TYPE_PARAMETER")
        val CONSTRUCTOR =
            createTextAttributesKey("OCAML_CONSTRUCTOR")
        val NAME_TAG =
            createTextAttributesKey("OCAML_NAME_TAG")
        val LABELED_PARAMETER =
            createTextAttributesKey("OCAML_LABELED_PARAMETER")
        val ATTRIBUTE =
            createTextAttributesKey("OCAML_ATTRIBUTE")
        val EXTENSION =
            createTextAttributesKey("OCAML_EXTENSION")

        val IDENTIFIER_KEYS = arrayOf(IDENTIFIER)
        val KEYWORD_KEYS = arrayOf(KEYWORD)
        val STRING_KEYS = arrayOf(STRING)
        val COMMENT_KEYS = arrayOf(COMMENT)
        val DOC_COMMENT_KEYS = arrayOf(DOC_COMMENT)
        val EMPTY_KEYS = emptyArray<TextAttributesKey>()
        val BRACKET_KEYS = arrayOf(BRACKET)
        val PAREN_KEYS = arrayOf(PAREN)
        val BRACE_KEYS = arrayOf(BRACE)
        val NUMBER_KEYS = arrayOf(NUMBER)
        val UIDENTIFIER_KEYS = arrayOf(UIDENTIFIER)
        val OPERATOR_KEYS = arrayOf(OPERATOR)
    }

    override fun getHighlightingLexer(): Lexer =
        FlexAdapter(_OcamlLexer(null))

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> =
        when (tokenType) {
            OcamlTypes.COMMENT -> COMMENT_KEYS
            OcamlTypes.DOC_COMMENT -> DOC_COMMENT_KEYS
            OcamlTypes.STRING -> STRING_KEYS
            in BRACKET_TOKENS -> BRACKET_KEYS
            in KEYWORD_TOKENS -> KEYWORD_KEYS
            in BRACE_TOKENS -> BRACE_KEYS
            in NUMBER_TOKENS -> NUMBER_KEYS
            in OPERATOR_TOKENS -> OPERATOR_KEYS
            OcamlTypes.RPAREN, OcamlTypes.LPAREN -> PAREN_KEYS
            OcamlTypes.LIDENT -> IDENTIFIER_KEYS
            OcamlTypes.UIDENT -> UIDENTIFIER_KEYS
            else -> EMPTY_KEYS
        }
}