package org.pcsoft.passat.language

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * A `program`/`unit` whose declared name does not match its file's base name must be flagged as a
 * warning (case-insensitively), and the offered quickfix renames the declaration to the file name.
 */
class ObjectPascalMismatchedNameTest : BasePlatformTestCase() {

    fun testMatchingProgramNameHasNoWarning() {
        myFixture.configureByText("Main.pas", "program Main;\nbegin\nend.\n")
        assertTrue(nameWarnings().isEmpty())
    }

    fun testCaseInsensitiveMatchHasNoWarning() {
        myFixture.configureByText("Main.pas", "program mAiN;\nbegin\nend.\n")
        assertTrue(nameWarnings().isEmpty())
    }

    fun testMismatchedProgramNameIsWarned() {
        myFixture.configureByText("Main.pas", "program Other;\nbegin\nend.\n")
        assertFalse("a mismatched program name must be flagged", nameWarnings().isEmpty())
    }

    fun testMismatchedUnitNameIsWarned() {
        myFixture.configureByText("Main.pas", "unit Other;\ninterface\nimplementation\nend.\n")
        assertFalse("a mismatched unit name must be flagged", nameWarnings().isEmpty())
    }

    fun testRenameProgramQuickfix() {
        myFixture.configureByText("Main.pas", "program Ot<caret>her;\nbegin\nend.\n")
        val fix = myFixture.findSingleIntention("Rename program to 'Main'")
        myFixture.launchAction(fix)
        assertEquals("program Main;\nbegin\nend.\n", myFixture.file.text)
        assertTrue(nameWarnings().isEmpty())
    }

    fun testRenameUnitQuickfix() {
        myFixture.configureByText("Main.pas", "unit Ot<caret>her;\ninterface\nimplementation\nend.\n")
        val fix = myFixture.findSingleIntention("Rename unit to 'Main'")
        myFixture.launchAction(fix)
        assertEquals("unit Main;\ninterface\nimplementation\nend.\n", myFixture.file.text)
        assertTrue(nameWarnings().isEmpty())
    }

    private fun nameWarnings() =
        myFixture.doHighlighting(HighlightSeverity.WARNING)
            .filter { it.description?.contains("does not match the file name") == true }
}
