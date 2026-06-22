package org.pcsoft.passat.plugin.sdk

import com.intellij.openapi.projectRoots.SdkAdditionalData
import org.jdom.Element

/**
 * Extra data attached to an FPC SDK: the target Object Pascal [PascalLanguageVersion] (dialect). It
 * is serialized into the SDK definition by [FpcSdkType.saveAdditionalData]/[FpcSdkType.loadAdditionalData].
 */
data class FpcSdkAdditionalData(
    var languageVersion: PascalLanguageVersion = PascalLanguageVersion.DEFAULT,
) : SdkAdditionalData {

    fun save(element: Element) {
        element.setAttribute(ATTR_LANGUAGE_VERSION, languageVersion.name)
    }

    companion object {
        private const val ATTR_LANGUAGE_VERSION = "languageVersion"

        fun load(element: Element): FpcSdkAdditionalData =
            FpcSdkAdditionalData(
                PascalLanguageVersion.fromNameOrDefault(element.getAttributeValue(ATTR_LANGUAGE_VERSION)),
            )
    }
}
