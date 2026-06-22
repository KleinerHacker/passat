package org.pcsoft.passat.language

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * The `uses` suggestion box must list every unit visible in scope — both project-local source units
 * and "standard" compiled units (here a `.ppu`, the form FPC SDK units ship in).
 */
class ObjectPascalUsesCompletionTest : BasePlatformTestCase() {

    fun testSuggestionBoxListsLocalAndStandardUnits() {
        // A project-local unit and a compiled (SDK-style) unit.
        myFixture.addFileToProject("LocalHelper.pas", "unit LocalHelper;\ninterface\nimplementation\nend.\n")
        myFixture.addFileToProject("system.ppu", "compiled-unit-stub")

        myFixture.configureByText("Main.pas", "program P;\nuses <caret>;\nbegin\nend.\n")
        myFixture.completeBasic()

        val suggestions = myFixture.lookupElementStrings ?: emptyList()
        assertContainsElements(suggestions, "LocalHelper", "system")
    }

    fun testProgramFilesAreNotOfferedAsUnits() {
        // .lpr is a program file, not an importable unit, and must not appear.
        myFixture.addFileToProject("Launcher.lpr", "program Launcher;\nbegin\nend.\n")
        myFixture.addFileToProject("RealUnit.pas", "unit RealUnit;\ninterface\nimplementation\nend.\n")

        myFixture.configureByText("Main.pas", "program P;\nuses <caret>;\nbegin\nend.\n")
        myFixture.completeBasic()

        val suggestions = myFixture.lookupElementStrings ?: emptyList()
        assertContainsElements(suggestions, "RealUnit")
        assertDoesntContain(suggestions, "Launcher")
    }
}
