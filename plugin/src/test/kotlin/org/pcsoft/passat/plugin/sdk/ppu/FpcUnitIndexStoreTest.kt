package org.pcsoft.passat.plugin.sdk.ppu

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.pcsoft.passat.plugin.sdk.FpcSdkUtil
import java.io.File

/**
 * Exercises the index core end to end: mapping a decoded unit, persisting/reloading it, mtime-based
 * invalidation, and — when a real `ppudump` is available on the machine — interpreting the genuine
 * `ctypes.ppu` fixture and indexing it through a synthesized FPC home.
 */
class FpcUnitIndexStoreTest {

    @JvmField
    @Rule
    val tempFolder = TemporaryFolder()

    private fun storeFile() = File(tempFolder.newFolder("idx"), "fpc-units.json")

    private fun resourceToFile(resource: String, dest: File): File {
        javaClass.classLoader.getResourceAsStream(resource)!!.use { input ->
            dest.outputStream().use { input.copyTo(it) }
        }
        return dest
    }

    /** A real ppudump on this machine, or null to skip the process-spawning tests on CI. */
    private fun realPpuDump(): File? =
        listOf("C:\\FPC", "/usr/lib/fpc", "/usr/local/lib/fpc", "/opt/fpc", "C:\\lazarus\\fpc")
            .flatMap { FpcSdkUtil.expandToValidHomes(it) }
            .firstNotNullOfOrNull { FpcSdkUtil.findPpuDumpExecutable(it) }

    @Test
    fun mapsDecodedUnitAndPersistsAcrossInstances() {
        val unit = PpuDumpReader.parse(
            javaClass.classLoader.getResourceAsStream("ppu/system.json")!!.bufferedReader().use { it.readText() }
        ).single()
        val ppu = File(tempFolder.newFolder("units"), "system.ppu").apply { writeText("x") }

        val file = storeFile()
        val store = FpcUnitIndexStore(file)
        store.put(ppu, unit)
        store.save()

        // Real, correctly-cased name + symbols are available immediately...
        assertEquals("System", store.internalUnitName(ppu))
        val symbols = store.symbolsOf(ppu)
        assertTrue("expected interface symbols", symbols.isNotEmpty())
        assertEquals("obj", symbols.first { it.name == "TObject" }.type)
        assertEquals("proc", symbols.first { it.name == "StringToOleStr" }.type)

        // ...and survive a reload from disk in a fresh store instance (cached, not re-read).
        assertTrue(file.isFile)
        val reloaded = FpcUnitIndexStore(file)
        assertEquals("System", reloaded.internalUnitName(ppu))
        assertEquals("TObject", reloaded.symbolsOf(ppu).first { it.name == "TObject" }.name)
    }

    @Test
    fun invalidatesEntryWhenPpuChanges() {
        val unit = PpuDumpReader.parse(
            javaClass.classLoader.getResourceAsStream("ppu/system.json")!!.bufferedReader().use { it.readText() }
        ).single()
        val ppu = File(tempFolder.newFolder("u2"), "system.ppu").apply { writeText("x") }

        val store = FpcUnitIndexStore(storeFile())
        store.put(ppu, unit)
        assertEquals("System", store.internalUnitName(ppu))

        // Touch the file so its lastModified no longer matches the stored stamp -> entry is stale.
        ppu.setLastModified(ppu.lastModified() + 5_000)
        assertNull("changed .ppu must not return a stale name", store.internalUnitName(ppu))
        assertTrue(store.symbolsOf(ppu).isEmpty())
    }

    @Test
    fun returnsNullForUnindexedFile() {
        val store = FpcUnitIndexStore(storeFile())
        val ppu = File(tempFolder.newFolder("u3"), "unknown.ppu").apply { writeText("x") }
        assertNull(store.internalUnitName(ppu))
        assertTrue(store.symbolsOf(ppu).isEmpty())
    }

    @Test
    fun interpretsRealDemoPpuViaPpudump() {
        val exe = realPpuDump()
        assumeTrue("no ppudump available on this machine", exe != null)

        val ppu = resourceToFile("ppu/ctypes.ppu", File(tempFolder.newFolder("real"), "ctypes.ppu"))
        val store = FpcUnitIndexStore(storeFile())

        assertTrue("ppudump should decode the demo unit", store.index(exe!!, ppu))
        assertEquals("ctypes", store.internalUnitName(ppu))
        val symbols = store.symbolsOf(ppu)
        assertTrue("ctypes should export symbols", symbols.isNotEmpty())
        assertNotNull("expected the pcint8 pointer type", symbols.firstOrNull { it.name == "pcint8" })
    }

    @Test
    fun ensureIndexedScansSynthesizedFpcHome() {
        val exe = realPpuDump()
        assumeTrue("no ppudump available on this machine", exe != null)

        // Build a minimal FPC home: bin/<ppudump> + units/<arch>/rtl/ctypes.ppu.
        val home = tempFolder.newFolder("fpc-home")
        val bin = File(home, "bin").apply { mkdirs() }
        exe!!.copyTo(File(bin, exe.name))
        val rtl = home.toPath().resolve("units").resolve("x86_64-win64").resolve("rtl").toFile().apply { mkdirs() }
        val ppu = resourceToFile("ppu/ctypes.ppu", File(rtl, "ctypes.ppu"))

        val store = FpcUnitIndexStore(storeFile())
        val count = store.ensureIndexed(home.absolutePath)
        assertTrue("at least the ctypes unit should be indexed", count >= 1)
        assertEquals("ctypes", store.internalUnitName(ppu))

        // A second run re-uses the cache (nothing changed) and indexes zero new units.
        assertEquals(0, store.ensureIndexed(home.absolutePath))
    }

    @Test
    fun ensureIndexedReturnsZeroWithoutPpudump() {
        // A home with no ppudump must not throw; nothing gets indexed.
        val home = tempFolder.newFolder("no-ppudump")
        assertEquals(0, FpcUnitIndexStore(storeFile()).ensureIndexed(home.absolutePath))
        assertFalse(FpcSdkUtil.hasPpuDump(home.absolutePath))
    }
}
