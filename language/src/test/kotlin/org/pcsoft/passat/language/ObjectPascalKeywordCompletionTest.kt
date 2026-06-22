package org.pcsoft.passat.language

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Keyword suggestions must appear only where they are grammatically meaningful:
 * `program` at the file root, `uses` after the program header (above the first `begin`), and
 * `end` inside an open `begin` block.
 */
class ObjectPascalKeywordCompletionTest : BasePlatformTestCase() {

    private fun completionsAt(text: String): List<String> {
        myFixture.configureByText("Main.pas", text)
        myFixture.completeBasic()
        return myFixture.lookupElementStrings ?: emptyList()
    }

    fun testProgramOfferedAtRootOfEmptyFile() {
        assertContainsElements(completionsAt("<caret>"), "program")
    }

    fun testProgramNotOfferedAfterHeader() {
        assertDoesntContain(completionsAt("program P;\n<caret>"), "program")
    }

    fun testUsesOfferedAfterProgramHeaderAboveBegin() {
        val suggestions = completionsAt("program P;\n<caret>\nbegin\nend.")
        assertContainsElements(suggestions, "uses")
        // `end` must not be offered outside the block.
        assertDoesntContain(suggestions, "end")
    }

    fun testUsesNotOfferedAtRoot() {
        assertDoesntContain(completionsAt("<caret>"), "uses")
    }

    fun testUsesNotOfferedInsideBlock() {
        assertDoesntContain(completionsAt("program P;\nbegin\n<caret>\nend."), "uses")
    }

    fun testUsesNotOfferedTwice() {
        // A program may have only one uses clause; do not offer it again afterwards.
        assertDoesntContain(completionsAt("program P;\nuses SysUtils;\n<caret>\nbegin\nend."), "uses")
    }

    fun testBeginOfferedAfterProgramHeader() {
        assertContainsElements(completionsAt("program P;\n<caret>"), "begin")
    }

    fun testBeginOfferedAfterLastUses() {
        val suggestions = completionsAt("program P;\nuses SysUtils;\n<caret>")
        assertContainsElements(suggestions, "begin")
        // `uses` is already present, so it must not be offered again here.
        assertDoesntContain(suggestions, "uses")
    }

    fun testBeginNotOfferedAtRoot() {
        assertDoesntContain(completionsAt("<caret>"), "begin")
    }

    fun testBeginNotOfferedInsideBlock() {
        assertDoesntContain(completionsAt("program P;\nbegin\n<caret>"), "begin")
    }

    fun testBeginNotOfferedInUnit() {
        assertDoesntContain(completionsAt("unit U;\ninterface\n<caret>"), "begin")
    }

    fun testEndOfferedInsideOpenBlock() {
        assertContainsElements(completionsAt("program P;\nbegin\n<caret>"), "end")
    }

    fun testEndNotOfferedBeforeBegin() {
        assertDoesntContain(completionsAt("program P;\n<caret>\nbegin\nend."), "end")
    }

    fun testUsesOfferedAfterUnitInterface() {
        assertContainsElements(completionsAt("unit U;\ninterface\n<caret>"), "uses")
    }

    fun testUsesOfferedAfterUnitImplementation() {
        assertContainsElements(completionsAt("unit U;\ninterface\nimplementation\n<caret>"), "uses")
    }
}
