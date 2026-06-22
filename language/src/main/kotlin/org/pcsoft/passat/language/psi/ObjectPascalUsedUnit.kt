package org.pcsoft.passat.language.psi

import com.intellij.psi.PsiElement

/**
 * A single unit imported through a `uses` clause (e.g. the `SysUtils` in `uses SysUtils;`). It
 * exposes the imported unit's name and the identifier element carrying it, and provides a
 * [com.intellij.psi.PsiReference] that resolves the name to the declaring unit file.
 */
interface ObjectPascalUsedUnit : PsiElement {
    /** The imported unit's name as written (e.g. `SysUtils`), or an empty string if absent. */
    fun getUnitName(): String

    /** The identifier element holding the unit name; the reference/highlight range. */
    fun getNameIdentifier(): PsiElement?
}
