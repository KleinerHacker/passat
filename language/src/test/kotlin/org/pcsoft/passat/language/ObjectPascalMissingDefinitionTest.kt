package org.pcsoft.passat.language

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * An empty Object Pascal file must be flagged as an error and offer quickfixes that fill it with a
 * complete `program` or `unit` skeleton named after the file.
 */
class ObjectPascalMissingDefinitionTest : BasePlatformTestCase() {

    fun testEmptyFileIsAnError() {
        myFixture.configureByText("Main.pas", "")
        val errors = myFixture.doHighlighting(HighlightSeverity.ERROR)
        assertFalse("expected an error on an empty Pascal file", errors.isEmpty())
    }

    fun testValidProgramHasNoMissingDefinitionError() {
        myFixture.configureByText("Main.pas", "program Main;\nbegin\nend.\n")
        val errors = myFixture.doHighlighting(HighlightSeverity.ERROR)
        assertTrue("a valid program must not be flagged", errors.isEmpty())
    }

    fun testCreateProgramQuickfixProducesValidProgram() {
        myFixture.configureByText("Foo.pas", "")
        val fix = myFixture.findSingleIntention("Create program skeleton")
        myFixture.launchAction(fix)

        assertEquals("program Foo;\n\nbegin\nend.\n", myFixture.file.text)
        assertTrue(myFixture.doHighlighting(HighlightSeverity.ERROR).isEmpty())
    }

    fun testCreateUnitQuickfixProducesValidUnit() {
        myFixture.configureByText("Foo.pas", "")
        val fix = myFixture.findSingleIntention("Create unit skeleton")
        myFixture.launchAction(fix)

        assertEquals("unit Foo;\n\ninterface\n\nimplementation\n\nend.\n", myFixture.file.text)
        assertTrue(myFixture.doHighlighting(HighlightSeverity.ERROR).isEmpty())
    }
}
