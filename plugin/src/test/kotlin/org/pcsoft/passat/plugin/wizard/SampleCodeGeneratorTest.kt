package org.pcsoft.passat.plugin.wizard

import org.junit.Assert.assertEquals
import org.junit.Test

class SampleCodeGeneratorTest {

    @Test
    fun keepsValidIdentifier() {
        assertEquals("MyProgram", SampleCodeGenerator.toIdentifier("MyProgram"))
    }

    @Test
    fun stripsIllegalCharacters() {
        assertEquals("myapp", SampleCodeGenerator.toIdentifier("my-app!"))
    }

    @Test
    fun prefixesLeadingDigit() {
        assertEquals("P1demo", SampleCodeGenerator.toIdentifier("1demo"))
    }

    @Test
    fun fallsBackForEmptyName() {
        assertEquals("Program1", SampleCodeGenerator.toIdentifier("***"))
    }

    @Test
    fun generatesProgramWithSysUtilsUses() {
        val source = SampleCodeGenerator.buildProgramSource("Demo")
        assertEquals(
            "program Demo;\n\nuses\n  SysUtils;\n\nbegin\nend.\n",
            source,
        )
    }
}
