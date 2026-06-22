package org.pcsoft.passat.plugin.sdk

import com.intellij.openapi.projectRoots.AdditionalDataConfigurable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkModificator
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.panel
import org.pcsoft.passat.plugin.i18n.PassatBundle
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

/**
 * UI for the extra FPC SDK data shown in the "Project Structure → SDKs" detail pane: a combo box to
 * pick the target Object Pascal [PascalLanguageVersion] (dialect).
 */
class FpcSdkAdditionalDataConfigurable(
    private val sdkModificator: SdkModificator,
) : AdditionalDataConfigurable {

    private var sdk: Sdk? = null

    private val versionCombo = ComboBox(DefaultComboBoxModel(PascalLanguageVersion.entries.toTypedArray())).apply {
        renderer = com.intellij.ui.SimpleListCellRenderer.create("") { it.displayName }
    }

    override fun setSdk(sdk: Sdk) {
        this.sdk = sdk
    }

    override fun createComponent(): JComponent = panel {
        row(PassatBundle.message("sdk.fpc.languageVersion.label")) {
            cell(versionCombo)
                .comment(PassatBundle.message("sdk.fpc.languageVersion.comment"))
        }
    }

    override fun isModified(): Boolean = versionCombo.selectedItem != currentData().languageVersion

    override fun apply() {
        val data = FpcSdkAdditionalData(versionCombo.selectedItem as PascalLanguageVersion)
        sdkModificator.sdkAdditionalData = data
    }

    override fun reset() {
        versionCombo.selectedItem = currentData().languageVersion
    }

    private fun currentData(): FpcSdkAdditionalData =
        sdkModificator.sdkAdditionalData as? FpcSdkAdditionalData ?: FpcSdkAdditionalData()
}
