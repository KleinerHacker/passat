package org.pcsoft.passat.language.intention

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import org.pcsoft.passat.language.psi.ObjectPascalFile
import org.pcsoft.passat.language.template.ObjectPascalFileTemplates

/**
 * Quickfix offered on an empty Object Pascal file: fills it with a complete, valid skeleton (see
 * [ObjectPascalFileTemplates]), naming it after the file. Only rewrites the editor document, so it
 * stays inside the reusable `:language` module (no project/toolchain context).
 */
sealed class CreatePascalSkeletonFix : IntentionAction {

    /** Builds the skeleton source for a file named [fileName] (without extension). */
    protected abstract fun source(fileName: String): String

    override fun getFamilyName(): String = "Create Object Pascal skeleton"

    override fun startInWriteAction(): Boolean = true

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean =
        editor != null && file is ObjectPascalFile && file.text.isBlank()

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (editor == null || file == null) return
        val fileName = file.virtualFile?.nameWithoutExtension ?: file.name.substringBeforeLast('.')
        val document = editor.document
        document.setText(source(fileName))
        PsiDocumentManager.getInstance(project).commitDocument(document)
    }
}

/** Fills an empty file with a minimal `program` skeleton. */
class CreateProgramSkeletonFix : CreatePascalSkeletonFix() {
    override fun getText(): String = "Create program skeleton"

    override fun source(fileName: String): String = ObjectPascalFileTemplates.programSource(fileName)
}

/** Fills an empty file with a minimal `unit` skeleton (empty interface/implementation sections). */
class CreateUnitSkeletonFix : CreatePascalSkeletonFix() {
    override fun getText(): String = "Create unit skeleton"

    override fun source(fileName: String): String = ObjectPascalFileTemplates.unitSource(fileName)
}
