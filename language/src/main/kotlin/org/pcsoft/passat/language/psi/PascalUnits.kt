package org.pcsoft.passat.language.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.pcsoft.passat.language.ObjectPascalFileType

/**
 * Single source of truth for "which units exist" within a given scope. A unit is identified by its
 * file's base name (FPC resolves units by file name, case-insensitively). Considered are Object
 * Pascal source units (`.pas`/`.pp`) and compiled units (`.ppu`); program files (`.lpr`) are not
 * importable and are excluded.
 *
 * This stays within core platform indexing APIs (no project-model/SDK types), so it can live in the
 * reusable `:language` core. Whether the FPC SDK's unit directories are part of [scope] is decided
 * by the `:plugin` module, which attaches them as SDK roots.
 */
object PascalUnits {
    private const val COMPILED_UNIT_EXT = "ppu"
    private const val PROGRAM_EXT = "lpr"

    private fun candidateFiles(project: Project, scope: GlobalSearchScope): Collection<VirtualFile> {
        val sources = FileTypeIndex.getFiles(ObjectPascalFileType.INSTANCE, scope)
            .filter { !it.extension.equals(PROGRAM_EXT, ignoreCase = true) }
        val compiled = FilenameIndex.getAllFilesByExt(project, COMPILED_UNIT_EXT, scope)
        return sources + compiled
    }

    /** An importable unit: its name and the file that provides it (a `.pas`/`.pp` or `.ppu`). */
    data class Unit(val name: String, val file: VirtualFile)

    /** All importable unit names visible in [scope], de-duplicated case-insensitively and sorted. */
    fun availableUnitNames(project: Project, scope: GlobalSearchScope): List<String> {
        val names = sortedSetOf(String.CASE_INSENSITIVE_ORDER)
        candidateFiles(project, scope).forEach { names.add(it.nameWithoutExtension) }
        return names.toList()
    }

    /**
     * All importable units visible in [scope] with their providing file, sorted by name and
     * de-duplicated case-insensitively (the first file wins when several share a name). Used by
     * completion to show each unit's originating file alongside its name.
     */
    fun availableUnits(project: Project, scope: GlobalSearchScope): List<Unit> {
        val byName = sortedMapOf<String, Unit>(String.CASE_INSENSITIVE_ORDER)
        candidateFiles(project, scope).forEach { file ->
            byName.putIfAbsent(file.nameWithoutExtension, Unit(file.nameWithoutExtension, file))
        }
        return byName.values.toList()
    }

    /** The file providing the unit named [name] in [scope], or `null` if none is visible. */
    fun findUnitFile(project: Project, name: String, scope: GlobalSearchScope): VirtualFile? =
        candidateFiles(project, scope).firstOrNull { it.nameWithoutExtension.equals(name, ignoreCase = true) }
}
