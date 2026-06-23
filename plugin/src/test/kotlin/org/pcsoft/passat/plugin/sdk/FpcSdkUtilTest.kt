package org.pcsoft.passat.plugin.sdk

import com.intellij.openapi.util.SystemInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FpcSdkUtilTest {

    @JvmField
    @Rule
    val tempFolder = TemporaryFolder()

    private fun exe(name: String) = if (SystemInfo.isWindows) "$name.exe" else name

    @Test
    fun detectsCompilerInBinSubdirectory() {
        val home = tempFolder.newFolder("fpc")
        val bin = home.toPath().resolve("bin").toFile()
        bin.mkdirs()
        java.io.File(bin, exe("fpc")).writeText("")

        assertTrue(FpcSdkUtil.isValidHome(home.absolutePath))
        assertNotNull(FpcSdkUtil.findCompilerExecutable(home.absolutePath))
    }

    @Test
    fun detectsCompilerDirectlyInHome() {
        val home = tempFolder.newFolder("fpc2")
        java.io.File(home, exe("ppcx64")).writeText("")

        assertTrue(FpcSdkUtil.isValidHome(home.absolutePath))
    }

    @Test
    fun detectsCrossCompilerInNestedArchSubdirectory() {
        // Mirrors the Windows installer layout: <home>\bin\i386-win32\ppcrossx64.exe
        val home = tempFolder.newFolder("fpc3")
        val arch = home.toPath().resolve("bin").resolve("i386-win32").toFile()
        arch.mkdirs()
        java.io.File(arch, exe("ppcrossx64")).writeText("")

        assertTrue(FpcSdkUtil.isValidHome(home.absolutePath))
    }

    @Test
    fun rejectsDirectoryWithoutCompiler() {
        val home = tempFolder.newFolder("empty")
        assertFalse(FpcSdkUtil.isValidHome(home.absolutePath))
        assertFalse(FpcSdkUtil.isValidHome(""))
    }

    @Test
    fun findsCompiledUnitDirectories() {
        // Mirrors the FPC layout: <home>/units/<target>/<package>/*.ppu
        val home = tempFolder.newFolder("fpc-units")
        val rtl = home.toPath().resolve("units").resolve("x86_64-win64").resolve("rtl").toFile()
        rtl.mkdirs()
        java.io.File(rtl, "system.ppu").writeText("")
        java.io.File(rtl, "sysutils.ppu").writeText("")
        // A directory without .ppu must be ignored.
        home.toPath().resolve("units").resolve("x86_64-win64").resolve("docs").toFile().mkdirs()

        val dirs = FpcSdkUtil.findUnitDirectories(home.absolutePath)
        assertTrue("rtl unit dir should be found", dirs.any { it.name == "rtl" })
        assertFalse("dir without .ppu should be skipped", dirs.any { it.name == "docs" })
    }

    @Test
    fun findsLibrarySourceDirectories() {
        val home = tempFolder.newFolder("fpc-src")
        val sysutils = home.toPath().resolve("source").resolve("rtl").resolve("objpas").toFile()
        sysutils.mkdirs()
        java.io.File(sysutils, "sysutils.pp").writeText("")

        val dirs = FpcSdkUtil.findSourceDirectories(home.absolutePath)
        assertTrue("objpas source dir should be found", dirs.any { it.name == "objpas" })
    }

    @Test
    fun returnsEmptyUnitDirsWhenNoUnitsTree() {
        val home = tempFolder.newFolder("fpc-bare")
        assertTrue(FpcSdkUtil.findUnitDirectories(home.absolutePath).isEmpty())
        assertTrue(FpcSdkUtil.findSourceDirectories(home.absolutePath).isEmpty())
        assertTrue(FpcSdkUtil.findUnitDirectories("").isEmpty())
    }

    @Test
    fun expandToValidHomesFindsVersionedSubdirectory() {
        // Mirrors the Windows installer layout: the user picks C:\FPC, but the real home is the
        // versioned subdir C:\FPC\3.2.2 holding bin\<arch>\ppcross*.exe.
        val base = tempFolder.newFolder("FPC")
        val arch = base.toPath().resolve("3.2.2").resolve("bin").resolve("i386-win32").toFile()
        arch.mkdirs()
        java.io.File(arch, exe("ppcrossx64")).writeText("")

        val homes = FpcSdkUtil.expandToValidHomes(base.absolutePath)
        assertEquals(1, homes.size)
        assertTrue(homes.single().endsWith("3.2.2"))
    }

    @Test
    fun expandToValidHomesReturnsBaseWhenItselfValid() {
        val home = tempFolder.newFolder("fpc-direct")
        val bin = home.toPath().resolve("bin").toFile()
        bin.mkdirs()
        java.io.File(bin, exe("fpc")).writeText("")

        assertEquals(listOf(home.absolutePath), FpcSdkUtil.expandToValidHomes(home.absolutePath))
    }

    @Test
    fun expandToValidHomesPrefersNewestVersionFirst() {
        val base = tempFolder.newFolder("FPC-multi")
        for (v in listOf("3.0.4", "3.2.2")) {
            val arch = base.toPath().resolve(v).resolve("bin").resolve("i386-win32").toFile()
            arch.mkdirs()
            java.io.File(arch, exe("ppcrossx64")).writeText("")
        }

        val homes = FpcSdkUtil.expandToValidHomes(base.absolutePath)
        assertEquals(2, homes.size)
        assertTrue(homes.first().endsWith("3.2.2"))
    }

    @Test
    fun expandToValidHomesEmptyForNonHome() {
        val empty = tempFolder.newFolder("nothing-here")
        assertTrue(FpcSdkUtil.expandToValidHomes(empty.absolutePath).isEmpty())
        assertTrue(FpcSdkUtil.expandToValidHomes("").isEmpty())
    }

    @Test
    fun detectVersionReturnsNullForMissingCompiler() {
        val home = tempFolder.newFolder("fpc-noexe")
        assertNull(FpcSdkUtil.detectVersion(home.absolutePath))
    }

    @Test
    fun detectVersionReturnsNullWhenCompilerCannotRun() {
        // A non-executable stub must not throw (e.g. no EDT/process assertions) — it returns null.
        val home = tempFolder.newFolder("fpc-badexe")
        val bin = home.toPath().resolve("bin").toFile()
        bin.mkdirs()
        java.io.File(bin, exe("fpc")).writeText("not a real executable")

        assertNull(FpcSdkUtil.detectVersion(home.absolutePath))
    }
}
