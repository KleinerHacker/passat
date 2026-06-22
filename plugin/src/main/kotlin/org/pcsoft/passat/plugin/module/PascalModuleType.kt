package org.pcsoft.passat.plugin.module

import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import org.pcsoft.passat.plugin.i18n.PassatBundle
import org.pcsoft.passat.plugin.icon.PassatIcons
import javax.swing.Icon

/**
 * Dedicated module type for Object Pascal modules, analogous to Java and Kotlin module types. Each
 * Pascal module carries a target FPC SDK (compiler), a target language version (on the SDK) and a
 * dependency list. Implementation lives in `:plugin` per the reuse-boundary rule.
 */
class PascalModuleType : ModuleType<PascalModuleBuilder>(ID) {

    override fun createModuleBuilder(): PascalModuleBuilder = PascalModuleBuilder()

    override fun getName(): String = PassatBundle.message("module.pascal.name")

    override fun getDescription(): String = PassatBundle.message("module.pascal.description")

    override fun getNodeIcon(isOpened: Boolean): Icon = PassatIcons.PascalModule

    companion object {
        const val ID = "PASCAL_MODULE"

        @JvmStatic
        fun getInstance(): PascalModuleType = ModuleTypeManager.getInstance().findByID(ID) as PascalModuleType
    }
}
