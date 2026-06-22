package org.pcsoft.passat.plugin.sdk

import com.intellij.openapi.util.SystemInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
}
