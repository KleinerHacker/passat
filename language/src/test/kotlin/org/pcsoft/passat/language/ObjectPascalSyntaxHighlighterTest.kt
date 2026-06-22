package org.pcsoft.passat.language

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.pcsoft.passat.language.highlight.ObjectPascalSyntaxHighlighter
import org.pcsoft.passat.language.parser.psi.ObjectPascalTypes

/**
 * Verifies that the Object Pascal syntax highlighter assigns the keyword color to the keyword
 * tokens (and nothing to the rest), independent of case. The keyword key falls back to the Java
 * keyword color, so this also guarantees the Java keyword highlight is wired up.
 */
class ObjectPascalSyntaxHighlighterTest : BasePlatformTestCase() {

    private val highlighter = ObjectPascalSyntaxHighlighter()

    fun testKeywordsGetKeywordColor() {
        val keywords = listOf(
            ObjectPascalTypes.PROGRAM,
            ObjectPascalTypes.UNIT,
            ObjectPascalTypes.USES,
            ObjectPascalTypes.INTERFACE,
            ObjectPascalTypes.IMPLEMENTATION,
            ObjectPascalTypes.IN,
            ObjectPascalTypes.BEGIN,
            ObjectPascalTypes.END,
        )
        for (keyword in keywords) {
            assertEquals(
                "Keyword $keyword should map to the keyword color",
                listOf(ObjectPascalSyntaxHighlighter.KEYWORD),
                keysFor(keyword),
            )
        }
    }

    fun testStringLiteralGetsStringColor() {
        assertEquals(
            listOf(ObjectPascalSyntaxHighlighter.STRING),
            keysFor(ObjectPascalTypes.STRING),
        )
    }

    /** A program with a `uses` clause highlights all of its keywords (`program`, `uses`, `begin`, `end`). */
    fun testHighlightingLexerScansUsesClause() {
        assertKeywordCount("program Demo;\nuses SysUtils;\nbegin\nend.\n", 4)
    }

    fun testNonKeywordsHaveNoHighlight() {
        for (token in listOf(ObjectPascalTypes.IDENTIFIER, ObjectPascalTypes.SEMICOLON, ObjectPascalTypes.DOT)) {
            assertEquals("Token $token should have no highlighting", emptyList<TextAttributesKey>(), keysFor(token))
        }
    }

    /** The keyword attribute key falls back to the (theme-aware) Java keyword color. */
    fun testKeywordKeyFallsBackToJavaKeywordColor() {
        assertEquals(
            com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD,
            ObjectPascalSyntaxHighlighter.KEYWORD.fallbackAttributeKey,
        )
    }

    /** Highlighting runs on the lexer, so an actual (lower- and upper-case) program highlights its keywords. */
    fun testHighlightingLexerScansProgram() {
        assertKeywordCount("program Demo;\nbegin\nend.\n", 3)
        assertKeywordCount("PROGRAM Demo; BEGIN END.", 3)
    }

    private fun assertKeywordCount(text: String, expected: Int) {
        val lexer = highlighter.highlightingLexer
        lexer.start(text)
        var count = 0
        while (lexer.tokenType != null) {
            if (highlighter.getTokenHighlights(lexer.tokenType!!)
                    .contains(ObjectPascalSyntaxHighlighter.KEYWORD)
            ) {
                count++
            }
            lexer.advance()
        }
        assertEquals("Highlighted keyword count in <$text>", expected, count)
    }

    private fun keysFor(tokenType: IElementType): List<TextAttributesKey> =
        highlighter.getTokenHighlights(tokenType).toList()
}
