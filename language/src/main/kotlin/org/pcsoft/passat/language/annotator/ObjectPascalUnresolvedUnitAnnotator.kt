package org.pcsoft.passat.language.annotator

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.pcsoft.passat.language.psi.ObjectPascalUsedUnit

/**
 * Flags an imported unit that cannot be resolved to any unit file in scope as an error, rendered in
 * the standard "wrong reference" red. Resolution covers project-local and FPC SDK units (see
 * [org.pcsoft.passat.language.psi.ObjectPascalUsedUnitReference]).
 */
class ObjectPascalUnresolvedUnitAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is ObjectPascalUsedUnit) return
        val nameElement = element.getNameIdentifier() ?: return
        val reference = element.reference ?: return
        if (reference.resolve() != null) return

        holder.newAnnotation(HighlightSeverity.ERROR, "Cannot resolve unit '${element.getUnitName()}'")
            .range(nameElement)
            .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
            .create()
    }
}
