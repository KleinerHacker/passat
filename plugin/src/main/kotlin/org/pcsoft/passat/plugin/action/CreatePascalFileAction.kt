package org.pcsoft.passat.plugin.action

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiDirectory
import org.pcsoft.passat.plugin.i18n.PassatBundle
import org.pcsoft.passat.plugin.icon.PassatIcons

/**
 * "New Object Pascal File" action. Like the Java "New Java Class" action it opens the platform's
 * frameless create-from-template popup (a name text field combined with a kind chooser) and lets
 * the user pick between a Unit and a Program, each backed by its own internal file template.
 */
class CreatePascalFileAction : CreateFileFromTemplateAction(
    PassatBundle.message("action.newPascalFile.text"),
    PassatBundle.message("action.newPascalFile.description"),
    PassatIcons.PascalFile,
) {
    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle(PassatBundle.message("action.newPascalFile.dialog.title"))
            .addKind(PassatBundle.message("action.newPascalFile.kind.unit"), PassatIcons.PascalFile, TEMPLATE_UNIT)
            .addKind(PassatBundle.message("action.newPascalFile.kind.program"), PassatIcons.PascalFile, TEMPLATE_PROGRAM)
    }

    override fun getActionName(directory: PsiDirectory, newName: String, templateName: String): String =
        PassatBundle.message("action.newPascalFile.text")

    /**
     * Restrict the action to source roots: it is only offered when at least one of the targeted
     * directories lies inside source content (the source folder itself or any directory below it).
     */
    override fun isAvailable(dataContext: DataContext): Boolean {
        if (!super.isAvailable(dataContext)) return false
        val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return false
        val view = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return false
        val index = ProjectFileIndex.getInstance(project)
        return view.directories.any { index.isInSourceContent(it.virtualFile) }
    }

    companion object {
        private const val TEMPLATE_UNIT = "Pascal Unit"
        private const val TEMPLATE_PROGRAM = "Pascal Program"
    }
}
