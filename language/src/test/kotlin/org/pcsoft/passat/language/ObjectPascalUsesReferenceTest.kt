package org.pcsoft.passat.language

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Resolution of `uses` imports and the error shown when a unit cannot be resolved. Covers both
 * project-local source units and standard compiled (`.ppu`) units, plus case-insensitivity.
 */
class ObjectPascalUsesReferenceTest : BasePlatformTestCase() {

    fun testResolvesLocalUnit() {
        val local = myFixture.addFileToProject("LocalHelper.pas", "unit LocalHelper;\ninterface\nimplementation\nend.\n")
        myFixture.configureByText("Main.pas", "program P;\nuses LocalHel<caret>per;\nbegin\nend.\n")

        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("expected a unit reference at the caret", reference)
        assertEquals(local, reference!!.resolve())
    }

    fun testResolvesStandardCompiledUnitCaseInsensitively() {
        val ppu = myFixture.addFileToProject("sysutils.ppu", "compiled-unit-stub")
        // Imported as `SysUtils`, the file is `sysutils.ppu` — FPC resolves units case-insensitively.
        myFixture.configureByText("Main.pas", "program P;\nuses SysUt<caret>ils;\nbegin\nend.\n")

        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull("expected a unit reference at the caret", reference)
        assertEquals(ppu, reference!!.resolve())
    }

    fun testUnresolvedUnitIsResolveNull() {
        myFixture.configureByText("Main.pas", "program P;\nuses No<caret>pe;\nbegin\nend.\n")
        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)
        assertNotNull(reference)
        assertNull(reference!!.resolve())
    }

    fun testUnresolvedUnitIsMarkedAsError() {
        myFixture.configureByText(
            "Main.pas",
            "program P;\nuses <error descr=\"Cannot resolve unit 'Nope'\">Nope</error>;\nbegin\nend.\n",
        )
        myFixture.checkHighlighting()
    }

    fun testResolvedUnitHasNoError() {
        myFixture.addFileToProject("LocalHelper.pas", "unit LocalHelper;\ninterface\nimplementation\nend.\n")
        myFixture.configureByText("Main.pas", "program P;\nuses LocalHelper;\nbegin\nend.\n")
        myFixture.checkHighlighting()
    }
}
