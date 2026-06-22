package org.pcsoft.passat.plugin.module

import com.intellij.openapi.module.ModuleConfigurationEditor
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState

/**
 * Contributes the extra "Pascal" tab to Project Structure → Modules for Pascal modules. The standard
 * "Sources"/"Dependencies" tabs (the order-entry-based dependency list, both internal module
 * dependencies and external unit-path libraries) are provided by the platform's default editor
 * provider, so they appear automatically for our module type.
 */
class PascalModuleConfigurationEditorProvider : ModuleConfigurationEditorProvider {

    override fun createEditors(state: ModuleConfigurationState): Array<ModuleConfigurationEditor> {
        val module = state.currentRootModel?.module ?: return emptyArray()
        if (ModuleType.get(module) !is PascalModuleType) return emptyArray()
        return arrayOf(PascalModuleEditor(state))
    }
}
