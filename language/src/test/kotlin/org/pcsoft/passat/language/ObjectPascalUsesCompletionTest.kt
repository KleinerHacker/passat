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

    fun testSuggestionsOfferedBeforeTerminatingSemicolon() {
        // While still typing the clause (the closing `;` is not there yet), unit suggestions must
        // already appear right after `uses` — waiting for the `;` would be useless for writing code.
        myFixture.addFileToProject("LocalHelper.pas", "unit LocalHelper;\ninterface\nimplementation\nend.\n")

        myFixture.configureByText("Main.pas", "program P;\nuses <caret>")
        myFixture.completeBasic()

        val suggestions = myFixture.lookupElementStrings ?: emptyList()
        assertContainsElements(suggestions, "LocalHelper")
    }

    fun testSuggestionsStillOfferedAfterAnExistingUnit() {
        // Pressing Ctrl+Space on a second unit (something already follows `uses`) must still list units.
        myFixture.addFileToProject("FirstUnit.pas", "unit FirstUnit;\ninterface\nimplementation\nend.\n")
        myFixture.addFileToProject("SecondUnit.pas", "unit SecondUnit;\ninterface\nimplementation\nend.\n")

        myFixture.configureByText("Main.pas", "program P;\nuses FirstUnit, <caret>;\nbegin\nend.\n")
        myFixture.completeBasic()

        val suggestions = myFixture.lookupElementStrings ?: emptyList()
        assertContainsElements(suggestions, "SecondUnit")
    }

    fun testSuggestionsOfferedWhenCaretInsideExistingUnit() {
        // Ctrl+Space while the caret sits inside an already-typed unit name must still suggest units
        // matching the prefix before the caret (regression: this previously returned nothing).
        myFixture.addFileToProject("SysUtils.pas", "unit SysUtils;\ninterface\nimplementation\nend.\n")
        myFixture.addFileToProject("SyncObjs.pas", "unit SyncObjs;\ninterface\nimplementation\nend.\n")
        myFixture.addFileToProject("Classes.pas", "unit Classes;\ninterface\nimplementation\nend.\n")

        // Caret after "Sy", inside the existing identifier "SysUtils".
        myFixture.configureByText("Main.pas", "program P;\nuses Sy<caret>sUtils;\nbegin\nend.\n")
        myFixture.completeBasic()

        val suggestions = myFixture.lookupElementStrings ?: emptyList()
        assertContainsElements(suggestions, "SysUtils", "SyncObjs")
        assertDoesntContain(suggestions, "Classes") // prefix "Sy" filters this out
    }

    fun testSuggestionBoxShowsOriginatingFileAsTail() {
        myFixture.addFileToProject("LocalHelper.pas", "unit LocalHelper;\ninterface\nimplementation\nend.\n")
        myFixture.addFileToProject("system.ppu", "compiled-unit-stub")

        myFixture.configureByText("Main.pas", "program P;\nuses <caret>;\nbegin\nend.\n")
        myFixture.completeBasic()

        val tails = myFixture.lookupElements.orEmpty().associate { element ->
            val presentation = com.intellij.codeInsight.lookup.LookupElementPresentation()
            element.renderElement(presentation)
            presentation.itemText to presentation.tailText
        }
        assertEquals("  LocalHelper.pas", tails["LocalHelper"])
        assertEquals("  system.ppu", tails["system"])
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
