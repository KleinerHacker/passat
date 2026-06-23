package org.pcsoft.passat.language.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.pcsoft.passat.language.psi.ObjectPascalFile

/**
 * Flags an Object Pascal file that declares neither a `program` nor a `unit` as an error. The
 * matching quickfixes that fill the file from a skeleton are registered as intention actions
 * ([org.pcsoft.passat.language.intention.CreateProgramSkeletonFix] /
 * [org.pcsoft.passat.language.intention.CreateUnitSkeletonFix]), so they remain reachable even on a
 * zero-length file. Scoped to the empty / whitespace-only case: once any content exists, the
 * grammar's own parse errors guide the user.
 */
class ObjectPascalMissingDefinitionAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is ObjectPascalFile) return
        if (!element.text.isBlank()) return

        holder.newAnnotation(HighlightSeverity.ERROR, "Pascal file must declare a program or a unit")
            .range(TextRange(0, element.textLength))
            .create()
    }
}
