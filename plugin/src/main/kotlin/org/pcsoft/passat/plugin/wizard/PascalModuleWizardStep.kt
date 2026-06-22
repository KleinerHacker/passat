package org.pcsoft.passat.plugin.wizard

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import org.pcsoft.passat.plugin.i18n.PassatBundle
import org.pcsoft.passat.plugin.module.PascalModuleBuilder
import javax.swing.JComponent

/**
 * Extra wizard step shown when creating a Pascal module. Currently offers the "With sample code"
 * option that generates a minimal starter program. The FPC SDK is selected on the platform's
 * standard SDK step, driven by [PascalModuleBuilder.isSuitableSdkType].
 */
class PascalModuleWizardStep(private val builder: PascalModuleBuilder) : ModuleWizardStep() {

    private var withSampleCode: Boolean = builder.withSampleCode

    private val panel = panel {
        row {
            checkBox(PassatBundle.message("wizard.pascal.sampleCode.checkbox"))
                .bindSelected(::withSampleCode)
        }
    }

    override fun getComponent(): JComponent = panel

    override fun updateDataModel() {
        panel.apply()
        builder.withSampleCode = withSampleCode
    }
}
