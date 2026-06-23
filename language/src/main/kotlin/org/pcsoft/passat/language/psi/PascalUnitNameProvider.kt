package org.pcsoft.passat.language.psi

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Lets a host module override the name under which a unit file is presented and matched. By default
 * [PascalUnits] derives a unit's name from its file's base name (e.g. `system.ppu` -> `system`).
 * Compiled units, however, carry a real, correctly-cased internal name that only a toolchain-aware
 * module can extract (Passat's `:plugin` reads it from the `.ppu` via `ppudump`). Such a module
 * contributes an implementation of this extension point; [PascalUnits] consults the registered
 * providers and falls back to the file-name derivation when none supplies a name.
 *
 * Declared in the reusable `:language` core (no project-model/SDK dependency); the implementation
 * lives wherever the toolchain context does.
 */
interface PascalUnitNameProvider {

    /** The display/match name for [file], or `null` to defer to other providers / the file name. */
    fun displayName(project: Project, file: VirtualFile): String?

    companion object {
        val EP_NAME: ExtensionPointName<PascalUnitNameProvider> =
            ExtensionPointName.create("org.pcsoft.passat.language.unitNameProvider")

        /** First non-null name any registered provider gives for [file], or `null`. */
        fun resolve(project: Project, file: VirtualFile): String? =
            EP_NAME.extensionList.firstNotNullOfOrNull { it.displayName(project, file) }
    }
}
