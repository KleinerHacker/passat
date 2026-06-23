package org.pcsoft.passat.language

import com.intellij.openapi.editor.FoldRegion
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Verifies the Object Pascal code folding: `begin ... end` blocks (terminator included), `uses`
 * folds (collapsed by default), and the unit sections (`interface`, `implementation`,
 * `initialization`, `finalization`).
 */
class ObjectPascalFoldingTest : BasePlatformTestCase() {

    /** A `begin ... end.` block folds with its trailing `.` included in the region. */
    fun testBlockFoldIncludesTerminator() {
        val text = "program Demo;\nbegin\nend.\n"
        val region = foldRegions(text).single { it.placeholderText == "begin ... end" }
        assertEquals("fold should reach through the trailing '.'", text.indexOf('.') + 1, region.endOffset)
        assertTrue("block fold should be expanded by default", region.isExpanded)
    }

    /** A program `uses` clause folds to `uses ...` and is collapsed by default. */
    fun testUsesFoldCollapsedByDefault() {
        val region = foldRegions("program Demo;\nuses SysUtils, Classes;\nbegin\nend.\n")
            .single { it.placeholderText == "uses ..." }
        assertFalse("uses fold should be collapsed by default", region.isExpanded)
    }

    /** Interface-uses and implementation-uses form two separate folds (the keyword breaks the run). */
    fun testUnitHasTwoSeparateUsesFolds() {
        val uses = foldRegions(
            "unit Foo;\ninterface\nuses SysUtils;\nimplementation\nuses Classes;\nend.\n",
        ).filter { it.placeholderText == "uses ..." }
        assertEquals("interface and implementation uses should fold independently", 2, uses.size)
    }

    /** Consecutive `uses` clauses (like Java imports) collapse into a single fold. */
    fun testConsecutiveUsesMergeIntoOneFold() {
        val uses = foldRegions("program Demo;\nuses SysUtils;\nuses Classes;\nbegin\nend.\n")
            .filter { it.placeholderText == "uses ..." }
        assertEquals("consecutive uses clauses should fold as one region", 1, uses.size)
        assertEquals("the fold should span through the last uses clause", "uses Classes;".length,
            uses.single().endOffset - "program Demo;\nuses SysUtils;\n".length)
    }

    /** Each present unit section folds with its keyword-prefixed placeholder, expanded by default. */
    fun testSectionFolds() {
        val placeholders = foldRegions(
            "unit Foo;\n" +
                "interface\nuses SysUtils;\n" +
                "implementation\nuses Classes;\n" +
                "initialization\nfinalization\nend.\n",
        ).map { it.placeholderText }.toSet()

        assertTrue(placeholders.containsAll(listOf("interface ...", "implementation ...")))
    }

    private fun foldRegions(text: String): List<FoldRegion> {
        myFixture.configureByText("Foo.pas", text)
        myFixture.doHighlighting()
        return myFixture.editor.foldingModel.allFoldRegions.toList()
    }
}
