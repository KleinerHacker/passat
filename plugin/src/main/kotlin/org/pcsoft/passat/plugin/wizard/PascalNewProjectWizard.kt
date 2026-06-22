package org.pcsoft.passat.plugin.wizard

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.GeneratorNewProjectWizard
import com.intellij.ide.wizard.GitNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardBaseData.Companion.baseData
import com.intellij.ide.wizard.NewProjectWizardBaseStep
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.ide.wizard.RootNewProjectWizardStep
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindSelected
import org.pcsoft.passat.plugin.i18n.PassatBundle
import org.pcsoft.passat.plugin.icon.PassatIcons
import org.pcsoft.passat.plugin.module.PascalModuleBuilder
import javax.swing.Icon

/**
 * Adds a "Pascal" template to the New Project wizard (left-hand generators list). Reuses the
 * platform's standard name/location and Git steps and contributes the "With sample code" option,
 * then materializes the project through [PascalModuleBuilder].
 */
class PascalNewProjectWizard : GeneratorNewProjectWizard {

    override val id: String = "Pascal"

    override val name: String = PassatBundle.message("module.pascal.name")

    override val icon: Icon = PassatIcons.PascalModule

    override fun createStep(context: WizardContext): NewProjectWizardStep =
        RootNewProjectWizardStep(context)
            .nextStep(::NewProjectWizardBaseStep)
            .nextStep(::GitNewProjectWizardStep)
            .nextStep(::PascalNewProjectAssetStep)

    /** Final step: the Pascal-specific options and the actual module creation. */
    private class PascalNewProjectAssetStep(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
        private val withSampleCodeProperty = propertyGraph.property(true)

        override fun setupUI(builder: Panel) {
            with(builder) {
                row {
                    checkBox(PassatBundle.message("wizard.pascal.sampleCode.checkbox"))
                        .bindSelected(withSampleCodeProperty)
                }
            }
        }

        override fun setupProject(project: Project) {
            val name = baseData?.name ?: return
            val basePath = baseData?.path ?: return
            val contentRoot = FileUtil.toSystemIndependentName("$basePath/$name")

            runWriteAction { VfsUtil.createDirectoryIfMissing(contentRoot) }

            val builder = PascalModuleBuilder().apply {
                this.name = name
                moduleFilePath = "$contentRoot/$name.iml"
                contentEntryPath = contentRoot
                withSampleCode = withSampleCodeProperty.get()
            }
            try {
                builder.commit(project)
            } catch (e: Exception) {
                logger<PascalNewProjectWizard>().warn("Failed to create Pascal module for project '$name'", e)
            }
        }
    }
}
