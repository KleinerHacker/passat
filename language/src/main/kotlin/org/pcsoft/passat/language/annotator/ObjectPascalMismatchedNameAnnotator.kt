package org.pcsoft.passat.language.annotator

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.elementType
import org.pcsoft.passat.language.parser.psi.ObjectPascalProgramDefinition
import org.pcsoft.passat.language.parser.psi.ObjectPascalTypes
import org.pcsoft.passat.language.parser.psi.ObjectPascalUnitDefinition

/**
 * Flags a `program`/`unit` whose declared name does not match its file's base name (matching is
 * case-insensitive, as Object Pascal is). FPC resolves units by file name, so a mismatch is at best
 * confusing and at worst breaks resolution; it is reported as a warning (yellow underline) rather
 * than an error. The offered quickfix renames the declaration to the file's base name (without
 * extension). Stays within the reusable `:language` core (no project/toolchain context).
 */
class ObjectPascalMismatchedNameAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val kind = when (element) {
            is ObjectPascalProgramDefinition -> "program"
            is ObjectPascalUnitDefinition -> "unit"
            else -> return
        }
        val nameElement = nameIdentifierOf(element) ?: return
        val fileName = element.containingFile?.virtualFile?.nameWithoutExtension ?: return
        if (fileName.isEmpty()) return
        if (nameElement.text.equals(fileName, ignoreCase = true)) return

        holder.newAnnotation(
            HighlightSeverity.WARNING,
            "The $kind name '${nameElement.text}' does not match the file name '$fileName'",
        )
            .range(nameElement)
            .withFix(RenameToFileNameFix(element, fileName, kind))
            .create()
    }

    private fun nameIdentifierOf(definition: PsiElement): PsiElement? =
        definition.children.firstOrNull { it.elementType == ObjectPascalTypes.IDENTIFIER }
            ?: definition.node.findChildByType(ObjectPascalTypes.IDENTIFIER)?.psi

    /** Renames a `program`/`unit` declaration's name to [fileName] (the file's base name). */
    private class RenameToFileNameFix(
        definition: PsiElement,
        private val fileName: String,
        private val kind: String,
    ) : IntentionAction {

        private val pointer: SmartPsiElementPointer<PsiElement> =
            SmartPointerManager.createPointer(definition)

        override fun getText(): String = "Rename $kind to '$fileName'"

        override fun getFamilyName(): String = "Rename to match file name"

        override fun startInWriteAction(): Boolean = true

        override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean =
            pointer.element != null

        override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
            val definition = pointer.element ?: return
            val nameElement = definition.children.firstOrNull { it.elementType == ObjectPascalTypes.IDENTIFIER }
                ?: definition.node.findChildByType(ObjectPascalTypes.IDENTIFIER)?.psi
                ?: return
            val document = definition.containingFile?.viewProvider?.document ?: return
            val range = nameElement.textRange
            document.replaceString(range.startOffset, range.endOffset, fileName)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }
    }
}
