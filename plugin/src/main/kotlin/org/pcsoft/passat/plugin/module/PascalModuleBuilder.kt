package org.pcsoft.passat.plugin.module

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import org.pcsoft.passat.plugin.icon.PassatIcons
import org.pcsoft.passat.plugin.i18n.PassatBundle
import org.pcsoft.passat.plugin.sdk.FpcSdkType
import org.pcsoft.passat.plugin.wizard.PascalModuleWizardStep
import org.pcsoft.passat.plugin.wizard.SampleCodeGenerator
import javax.swing.Icon

/**
 * Builds a new Pascal module: lays out a `src` source root, binds the chosen FPC SDK as the module
 * compiler and, when requested, drops in a minimal starter program.
 */
class PascalModuleBuilder : ModuleBuilder() {

    /** Whether to generate a minimal starter program; set from [PascalModuleWizardStep]. */
    var withSampleCode: Boolean = true

    override fun getModuleType(): ModuleType<*> = PascalModuleType.getInstance()

    override fun getNodeIcon(): Icon = PassatIcons.PascalModule

    override fun getPresentableName(): String = PassatBundle.message("module.pascal.presentable.name")

    override fun getDescription(): String = PassatBundle.message("module.pascal.description")

    /** Restrict the wizard's SDK selector to FPC SDKs, mirroring how Java modules show JDKs. */
    override fun isSuitableSdkType(sdkType: SdkTypeId): Boolean = sdkType is FpcSdkType

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider,
    ): Array<ModuleWizardStep> = arrayOf(PascalModuleWizardStep(this))

    override fun setupRootModel(rootModel: ModifiableRootModel) {
        myJdk?.let { rootModel.sdk = it } ?: rootModel.inheritSdk()

        val contentEntryPath = contentEntryPath ?: return
        val contentEntry = doAddContentEntry(rootModel) ?: return

        val srcPath = FileUtil.toSystemIndependentName("$contentEntryPath/src")
        val sourceRoot = VfsUtil.createDirectoryIfMissing(srcPath) ?: return
        contentEntry.addSourceFolder(sourceRoot, false)

        if (withSampleCode) {
            SampleCodeGenerator.generate(sourceRoot, rootModel.module.name)
        }
    }
}
