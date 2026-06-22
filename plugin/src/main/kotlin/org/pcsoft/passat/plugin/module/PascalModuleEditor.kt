package org.pcsoft.passat.plugin.module

import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState
import com.intellij.openapi.roots.ui.configuration.ModuleElementsEditor
import com.intellij.ui.dsl.builder.panel
import org.pcsoft.passat.plugin.i18n.PassatBundle
import org.pcsoft.passat.plugin.sdk.FpcSdkAdditionalData
import org.pcsoft.passat.plugin.sdk.FpcSdkType
import javax.swing.JComponent
import javax.swing.JLabel

/**
 * "Pascal" tab in Project Structure → Modules. Surfaces the FPC SDK bound to the module (the target
 * compiler) and its target language version. The compiler and dependency lists are edited on the
 * platform's standard "Dependencies" tab via the order-entry model.
 */
class PascalModuleEditor(state: ModuleConfigurationState) : ModuleElementsEditor(state) {

    private val compilerLabel = JLabel()
    private val languageVersionLabel = JLabel()

    override fun getDisplayName(): String = PassatBundle.message("module.editor.pascal.tab")

    override fun createComponentImpl(): JComponent {
        val component = panel {
            row(PassatBundle.message("sdk.fpc.type.name") + ":") { cell(compilerLabel) }
            row(PassatBundle.message("sdk.fpc.languageVersion.label")) { cell(languageVersionLabel) }
        }
        refresh()
        return component
    }

    override fun reset() {
        super.reset()
        refresh()
    }

    override fun moduleStateChanged() {
        refresh()
    }

    private fun refresh() {
        val sdk = model?.sdk
        if (sdk != null && FpcSdkType.isFpcSdk(sdk.sdkType)) {
            compilerLabel.text = sdk.name
            val data = sdk.sdkAdditionalData as? FpcSdkAdditionalData
            languageVersionLabel.text = (data?.languageVersion ?: org.pcsoft.passat.plugin.sdk.PascalLanguageVersion.DEFAULT).displayName
        } else {
            compilerLabel.text = "<none>"
            languageVersionLabel.text = "-"
        }
    }

    override fun saveData() {}
}
