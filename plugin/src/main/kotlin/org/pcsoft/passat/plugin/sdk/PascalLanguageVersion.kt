package org.pcsoft.passat.plugin.sdk

/**
 * Object Pascal dialect level the FPC compiler is invoked with (the `-M<mode>` switch). This is the
 * "target language version" carried by the Pascal module via its FPC SDK, analogous to a language
 * level in Java.
 */
enum class PascalLanguageVersion(val displayName: String, val fpcMode: String) {
    FPC("FPC (default)", "fpc"),
    OBJFPC("Object Pascal (objfpc)", "objfpc"),
    DELPHI("Delphi", "delphi"),
    TP("Turbo Pascal", "tp"),
    MACPAS("Mac Pascal", "macpas"),
    ISO("ISO Pascal", "iso");

    companion object {
        val DEFAULT: PascalLanguageVersion = OBJFPC

        /** Resolves a dialect from a persisted name, falling back to [DEFAULT] for unknown input. */
        fun fromNameOrDefault(name: String?): PascalLanguageVersion =
            entries.firstOrNull { it.name == name } ?: DEFAULT

        /**
         * Picks a sensible default dialect for a detected FPC version. FPC defaults to the `objfpc`
         * mode in practice, so this returns [OBJFPC] regardless of version for now; the hook exists
         * so version-specific defaults can be added later.
         */
        @Suppress("UNUSED_PARAMETER")
        fun defaultFor(fpcVersion: String?): PascalLanguageVersion = DEFAULT
    }
}
