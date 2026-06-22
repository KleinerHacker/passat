package org.pcsoft.passat.language.psi

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

/**
 * Resolves an imported unit name to the file that provides it. Available units are taken from the
 * importing file's resolve scope (module sources + dependencies + SDK roots), so both project-local
 * `.pas`/`.pp` units and the FPC SDK's compiled `.ppu` / source units resolve. A hard (non-soft)
 * reference: an unresolved unit is reported as an error by
 * [org.pcsoft.passat.language.annotator.ObjectPascalUnresolvedUnitAnnotator].
 */
class ObjectPascalUsedUnitReference(
    owner: ObjectPascalUsedUnit,
    nameElement: PsiElement,
) : PsiReferenceBase<ObjectPascalUsedUnit>(
    owner,
    TextRange.from(nameElement.startOffsetInParent, nameElement.textLength),
    /* soft = */ false,
) {
    private val unitName: String = nameElement.text

    override fun resolve(): PsiElement? {
        val file = PascalUnits.findUnitFile(element.project, unitName, element.resolveScope) ?: return null
        return element.manager.findFile(file)
    }

    override fun getVariants(): Array<Any> =
        PascalUnits.availableUnitNames(element.project, element.resolveScope)
            .map { LookupElementBuilder.create(it) }
            .toTypedArray()
}
