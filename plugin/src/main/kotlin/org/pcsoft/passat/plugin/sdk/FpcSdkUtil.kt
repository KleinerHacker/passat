package org.pcsoft.passat.plugin.sdk

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.SystemInfo
import java.io.File
import java.util.concurrent.TimeUnit

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
     * Returns the `ppudump` executable under the given home, or `null` if none exists. `ppudump`
     * decodes compiled `.ppu` units (we use its `-Fjson` mode) and is required for Passat to read a
     * unit's real internal name and interface symbols. It lives next to the compiler, so we probe
     * the same roots (`<home>`, `<home>/bin`, `<home>/bin/<arch>`). The executable is `ppudump.exe`
     * on Windows and `ppudump` elsewhere via [exeName].
     */
    fun findPpuDumpExecutable(home: String): File? {
        if (home.isBlank()) return null
        val bin = File(home, "bin")
        val roots = buildList {
            add(File(home))
            add(bin)
            bin.listFiles { f -> f.isDirectory }?.let { addAll(it) }
        }
        val target = exeName("ppudump")
        for (root in roots) {
            val candidate = File(root, target)
            if (candidate.isFile) return candidate
        }
        return null
    }

    /** True if the `ppudump` tool is present under the given FPC home. */
    fun hasPpuDump(home: String): Boolean = findPpuDumpExecutable(home) != null

    /**
     * Resolves a base directory to the FPC homes it (transitively) contains. If [base] is itself a
     * valid home it is returned as-is; otherwise its immediate subdirectories are probed, which
     * covers the common versioned layout `C:\FPC\<version>` (the Windows installer puts the real
     * home one level below the directory the user picks). Returns an empty list when nothing valid
     * is found.
     */
    fun expandToValidHomes(base: String): List<String> {
        if (base.isBlank()) return emptyList()
        if (isValidHome(base)) return listOf(base)
        val dir = File(base)
        if (!dir.isDirectory) return emptyList()
        return dir.listFiles { f -> f.isDirectory }
            ?.map { it.absolutePath }
            ?.filter { isValidHome(it) }
            ?.sortedDescending() // prefer the newest version directory first
            ?: emptyList()
    }

    /**
     * Directories holding compiled units (`.ppu`) shipped with the installation, i.e. the RTL and
     * bundled packages. The FPC layout nests these under `<home>/units/<target>/<package>/`, so we
     * return every directory in that subtree that actually contains at least one `.ppu`. These are
     * attached as SDK class roots so a Pascal module can resolve standard units like `SysUtils`.
     */
    fun findUnitDirectories(home: String): List<File> {
        if (home.isBlank()) return emptyList()
        val unitsRoot = File(home, "units")
        if (!unitsRoot.isDirectory) return emptyList()
        return unitsRoot.walkTopDown()
            .filter { it.isDirectory && it.listFiles { _, n -> n.endsWith(".ppu", ignoreCase = true) }?.isNotEmpty() == true }
            .toList()
    }

    /**
     * Source directories of the standard library (`<home>/source/...`), attached as SDK source roots
     * so resolved standard units can navigate to their `.pas`/`.pp` source. Returns the leaf
     * directories that contain Object Pascal sources, or an empty list when no `source` tree exists.
     */
    fun findSourceDirectories(home: String): List<File> {
        if (home.isBlank()) return emptyList()
        val sourceRoot = File(home, "source")
        if (!sourceRoot.isDirectory) return emptyList()
        return sourceRoot.walkTopDown()
            .filter { dir ->
                dir.isDirectory && dir.listFiles { _, n ->
                    n.endsWith(".pas", ignoreCase = true) || n.endsWith(".pp", ignoreCase = true)
                }?.isNotEmpty() == true
            }
            .toList()
    }

    /**
     * Detects the FPC version by running `fpc -iV` (prints just the compiler version). Returns the
     * trimmed version string (e.g. `3.2.2`) or `null` if detection fails.
     *
     * Uses a plain [ProcessBuilder] rather than the platform's `ExecUtil`/`OSProcessHandler`: the
     * latter asserts it is not called on the EDT, but [com.intellij.openapi.projectRoots.SdkType]
     * version/name hooks are invoked synchronously on the EDT while adding an SDK. The `-iV` call
     * returns almost instantly; a short timeout guards against a hung process.
     */
    fun detectVersion(home: String): String? {
        val exe = findCompilerExecutable(home) ?: return null
        return try {
            val process = ProcessBuilder(exe.absolutePath, "-iV")
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                return null
            }
            if (process.exitValue() == 0) output.trim().ifEmpty { null } else null
        } catch (e: Exception) {
            LOG.info("Failed to detect FPC version for home '$home'", e)
            null
        }
    }
}
