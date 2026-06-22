package org.pcsoft.passat.plugin.sdk

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.SystemInfo
import java.io.File

/**
 * Pure helpers for locating and interrogating a Free Pascal Compiler installation. Kept free of any
 * IntelliJ SDK-model state so the logic can be unit-tested and reused.
 */
object FpcSdkUtil {
    private val LOG = logger<FpcSdkUtil>()

    /**
     * Candidate compiler executable names: the generic `fpc` driver first (preferred), then the
     * common native and cross target compilers (`ppc*`).
     */
    private val EXECUTABLE_NAMES = listOf(
        "fpc",
        "ppcx64", "ppcx86_64", "ppc386", "ppca64", "ppcarm", "ppcaarch64",
        "ppcrossx64", "ppcross386", "ppcrossa64", "ppcrossarm",
    )

    private fun exeName(base: String): String = if (SystemInfo.isWindows) "$base.exe" else base

    /**
     * Returns the FPC compiler executable under the given home directory, or `null` if none exists.
     * The Windows installer nests the binaries one level deeper in an architecture folder
     * (`<home>\bin\i386-win32\`), so besides `<home>` and `<home>\bin` we also scan the immediate
     * subdirectories of `<home>\bin`.
     */
    fun findCompilerExecutable(home: String): File? {
        if (home.isBlank()) return null
        val bin = File(home, "bin")
        val roots = buildList {
            add(File(home))
            add(bin)
            bin.listFiles { f -> f.isDirectory }?.let { addAll(it) }
        }
        // Probe by preference order (fpc driver first), across all candidate roots.
        for (name in EXECUTABLE_NAMES) {
            for (root in roots) {
                val candidate = File(root, exeName(name))
                if (candidate.isFile) return candidate
            }
        }
        return null
    }

    /** A path is a valid FPC home when a compiler executable can be located under it. */
    fun isValidHome(home: String): Boolean = findCompilerExecutable(home) != null

    /**
     * Detects the FPC version by running `fpc -iV` (prints just the compiler version). Returns the
     * trimmed version string (e.g. `3.2.2`) or `null` if detection fails.
     */
    fun detectVersion(home: String): String? {
        val exe = findCompilerExecutable(home) ?: return null
        return try {
            val commandLine = GeneralCommandLine(exe.absolutePath, "-iV")
            val output = ExecUtil.execAndGetOutput(commandLine, 5_000)
            if (output.exitCode == 0) output.stdout.trim().ifEmpty { null } else null
        } catch (e: Exception) {
            LOG.info("Failed to detect FPC version for home '$home'", e)
            null
        }
    }
}
