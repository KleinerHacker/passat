package org.pcsoft.passat.language.highlight

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.pcsoft.passat.language.lexer.ObjectPascalLexerAdapter
import org.pcsoft.passat.language.parser.psi.ObjectPascalTypes

/**
 * Syntax highlighting for Object Pascal.
 *
 * The keyword attribute is declared explicitly, but falls back to
 * [DefaultLanguageHighlighterColors.KEYWORD] - the same color key Java keywords resolve to - so the
 * keyword color matches Java's and adapts automatically to both dark and light color schemes.
 */
class ObjectPascalSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = ObjectPascalLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
        ObjectPascalTypes.PROGRAM,
        ObjectPascalTypes.UNIT,
        ObjectPascalTypes.USES,
        ObjectPascalTypes.INTERFACE,
        ObjectPascalTypes.IMPLEMENTATION,
        ObjectPascalTypes.INITIALIZATION,
        ObjectPascalTypes.FINALIZATION,
        ObjectPascalTypes.IN,
        ObjectPascalTypes.BEGIN,
        ObjectPascalTypes.END -> KEYWORD_KEYS
        ObjectPascalTypes.STRING -> STRING_KEYS
        TokenType.BAD_CHARACTER -> BAD_CHARACTER_KEYS
        else -> EMPTY_KEYS
    }

    companion object {
        val KEYWORD: TextAttributesKey =
            createTextAttributesKey("PASSAT_PASCAL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val STRING: TextAttributesKey =
            createTextAttributesKey("PASSAT_PASCAL_STRING", DefaultLanguageHighlighterColors.STRING)
        val BAD_CHARACTER: TextAttributesKey =
            createTextAttributesKey("PASSAT_PASCAL_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val STRING_KEYS = arrayOf(STRING)
        private val BAD_CHARACTER_KEYS = arrayOf(BAD_CHARACTER)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()
    }
}
