package org.pcsoft.passat.plugin.sdk

import org.junit.Assert.assertEquals
import org.junit.Test

class PascalLanguageVersionTest {

    @Test
    fun knownNameRoundTrips() {
        assertEquals(PascalLanguageVersion.DELPHI, PascalLanguageVersion.fromNameOrDefault("DELPHI"))
    }

    @Test
    fun unknownNameFallsBackToDefault() {
        assertEquals(PascalLanguageVersion.DEFAULT, PascalLanguageVersion.fromNameOrDefault("NOPE"))
        assertEquals(PascalLanguageVersion.DEFAULT, PascalLanguageVersion.fromNameOrDefault(null))
    }

    @Test
    fun defaultForAnyVersionIsDefault() {
        assertEquals(PascalLanguageVersion.DEFAULT, PascalLanguageVersion.defaultFor("3.2.2"))
        assertEquals(PascalLanguageVersion.DEFAULT, PascalLanguageVersion.defaultFor(null))
    }
}
