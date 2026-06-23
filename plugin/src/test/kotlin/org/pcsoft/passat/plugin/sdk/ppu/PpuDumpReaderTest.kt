package org.pcsoft.passat.plugin.sdk.ppu

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies that real `ppudump -Fjson` output is faithfully bound to the Kotlin model. The fixture
 * `ppu/system.json` is a trimmed-but-genuine dump of FPC's `system.ppu`, so a passing test proves
 * the JSON structure ppudump emits maps onto [PpuDumpUnit]/[PpuSymbol] as expected.
 */
class PpuDumpReaderTest {

    private fun sample(): String =
        javaClass.classLoader.getResourceAsStream("ppu/system.json")!!
            .bufferedReader().use { it.readText() }

    @Test
    fun parsesUnitHeaderFromRealDump() {
        val units = PpuDumpReader.parse(sample())
        assertEquals(1, units.size)
        val unit = units.single()

        // The real, correctly-cased internal name — not the lower-cased file name.
        assertEquals("System", unit.name)
        assertEquals("x86_64", unit.targetCpu)
        assertEquals("Win64-x64", unit.targetOs)
        assertEquals("5F9D7A80", unit.crc)
        assertTrue("source files should be populated", unit.files.isNotEmpty())
        assertEquals("system.pp", unit.files.first().name)
    }

    @Test
    fun parsesInterfaceSymbols() {
        val unit = PpuDumpReader.parse(sample()).single()
        val byName = unit.interfaceSymbols.associateBy { it.name }

        // A representative const, type, var, function/procedure and class are all bound.
        assertEquals("const", byName["True"]?.type)
        assertEquals("type", byName["Extended"]?.type)
        assertEquals("var", byName["LibModuleList"]?.type)
        assertEquals("proc", byName["StringToOleStr"]?.type)
        assertEquals("obj", byName["TObject"]?.type)

        // Symbol positions are decoded too.
        val pos = byName["LibModuleList"]?.pos
        assertNotNull(pos)
        assertEquals(3, pos!!.file)
    }

    @Test
    fun toleratesNonFiniteFloatLiterals() {
        // Some FPC units (e.g. math) dump float consts as bare `Nan`/`Inf` tokens, which are not
        // valid JSON. Parsing must not choke on them; the const symbol is still bound by name.
        val json = """
            [ { "Type": "unit", "Name": "Math",
                "Interface": [
                  { "Type": "const", "Name": "NaNConst", "Value": Nan },
                  { "Type": "const", "Name": "InfConst", "Value": -Inf }
                ] } ]
        """.trimIndent()
        val unit = PpuDumpReader.parse(json).single()
        assertEquals("Math", unit.name)
        val names = unit.interfaceSymbols.mapNotNull { it.name }
        assertTrue(names.containsAll(listOf("NaNConst", "InfConst")))
    }

    @Test
    fun ignoresUnknownAndMissingProperties() {
        // Symbols carry many type-specific fields we do not model; parsing must not fail on them,
        // and entries without a Name must still parse (name == null).
        val unit = PpuDumpReader.parse(sample()).single()
        assertTrue(unit.interfaceSymbols.isNotEmpty())
    }
}
