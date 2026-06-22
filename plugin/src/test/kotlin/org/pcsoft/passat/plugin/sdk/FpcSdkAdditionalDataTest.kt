package org.pcsoft.passat.plugin.sdk

import org.jdom.Element
import org.junit.Assert.assertEquals
import org.junit.Test

class FpcSdkAdditionalDataTest {

    @Test
    fun savedDataLoadsBackIdentically() {
        val original = FpcSdkAdditionalData(PascalLanguageVersion.DELPHI)
        val element = Element("additional")
        original.save(element)

        val loaded = FpcSdkAdditionalData.load(element)
        assertEquals(original.languageVersion, loaded.languageVersion)
    }

    @Test
    fun loadingMissingAttributeYieldsDefault() {
        val loaded = FpcSdkAdditionalData.load(Element("additional"))
        assertEquals(PascalLanguageVersion.DEFAULT, loaded.languageVersion)
    }
}
