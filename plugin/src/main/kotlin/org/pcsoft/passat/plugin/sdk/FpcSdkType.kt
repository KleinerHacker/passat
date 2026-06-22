package org.pcsoft.passat.plugin.sdk

import com.intellij.openapi.projectRoots.AdditionalDataConfigurable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkAdditionalData
import com.intellij.openapi.projectRoots.SdkModel
import com.intellij.openapi.projectRoots.SdkModificator
import com.intellij.openapi.projectRoots.SdkType
import com.intellij.openapi.projectRoots.SdkTypeId
import org.jdom.Element
import org.pcsoft.passat.plugin.i18n.PassatBundle
import org.pcsoft.passat.plugin.icon.PassatIcons
import javax.swing.Icon

/**
 * Registers a Free Pascal Compiler installation as a first-class IntelliJ SDK, exactly like the JDK
 * for Java. A Pascal module selects an FPC SDK as its target compiler via [com.intellij.openapi.roots.ModuleRootManager],
 * and the target language version (dialect) is carried as [FpcSdkAdditionalData].
 */
@Suppress("OVERRIDE_DEPRECATION")
class FpcSdkType : SdkType("FPC") {

    override fun getPresentableName(): String = PassatBundle.message("sdk.fpc.type.name")

    override fun getIcon(): Icon = PassatIcons.FpcSdk

    override fun getIconForAddAction(): Icon = PassatIcons.FpcSdk

    override fun suggestHomePath(): String? = suggestHomePaths().firstOrNull()

    override fun suggestHomePaths(): Collection<String> {
        val candidates = listOf(
            "/usr/lib/fpc",
            "/usr/local/lib/fpc",
            "/opt/fpc",
            "C:\\FPC",
            "C:\\lazarus\\fpc",
        )
        return candidates.filter { FpcSdkUtil.isValidHome(it) }
    }

    override fun isValidSdkHome(path: String): Boolean = FpcSdkUtil.isValidHome(path)

    override fun getInvalidHomeMessage(path: String): String = PassatBundle.message("sdk.fpc.home.invalid")

    override fun getVersionString(sdkHome: String): String? = FpcSdkUtil.detectVersion(sdkHome)

    override fun suggestSdkName(currentSdkName: String?, sdkHome: String): String {
        val version = FpcSdkUtil.detectVersion(sdkHome)
        return if (version != null) "FPC $version" else "FPC"
    }

    override fun createAdditionalDataConfigurable(
        sdkModel: SdkModel,
        sdkModificator: SdkModificator,
    ): AdditionalDataConfigurable = FpcSdkAdditionalDataConfigurable(sdkModificator)

    override fun saveAdditionalData(additionalData: SdkAdditionalData, additional: Element) {
        (additionalData as? FpcSdkAdditionalData)?.save(additional)
    }

    override fun loadAdditionalData(currentSdk: Sdk, additional: Element): SdkAdditionalData =
        FpcSdkAdditionalData.load(additional)

    companion object {
        @JvmStatic
        fun getInstance(): FpcSdkType = findInstance(FpcSdkType::class.java)

        /** True if the given SDK type id is the FPC SDK. */
        fun isFpcSdk(sdkTypeId: SdkTypeId?): Boolean = sdkTypeId is FpcSdkType
    }
}
