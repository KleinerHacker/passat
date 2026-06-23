package org.pcsoft.passat.language.completion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.pcsoft.passat.language.psi.PascalUnits

/**
 * Builds the `uses` suggestion entries. Each entry shows the unit name plus, as grey tail text, the
 * file that actually provides it (e.g. `system.ppu` or `MyUnit.pas`), so the user can tell project
 * units, SDK source units and compiled SDK units apart at a glance. Shared by the standalone
 * completion contributor and the unit reference's variants so both render identically.
 */
object UnitLookupElements {

    fun forScope(project: Project, scope: GlobalSearchScope): List<LookupElement> =
        PascalUnits.availableUnits(project, scope).map { unit ->
            LookupElementBuilder.create(unit.name)
                .withTailText("  ${unit.file.name}", /* grayed = */ true)
        }
}
