package org.pcsoft.passat.language.template

/**
 * Produces complete, valid Object Pascal skeleton sources used by the "create program/unit"
 * quickfixes on an empty file. The program/unit name is derived from the file name (without its
 * extension) and sanitized to a valid Pascal identifier.
 */
object ObjectPascalFileTemplates {

    /**
     * A minimal but valid program:
     * ```
     * program <name>;
     *
     * begin
     * end.
     * ```
     */
    fun programSource(fileName: String): String {
        val identifier = toIdentifier(fileName, fallback = "Program1")
        return buildString {
            append("program ").append(identifier).append(";\n")
            append("\n")
            append("begin\n")
            append("end.\n")
        }
    }

    /**
     * A minimal but valid unit with empty interface and implementation sections:
     * ```
     * unit <name>;
     *
     * interface
     *
     * implementation
     *
     * end.
     * ```
     */
    fun unitSource(fileName: String): String {
        val identifier = toIdentifier(fileName, fallback = "Unit1")
        return buildString {
            append("unit ").append(identifier).append(";\n")
            append("\n")
            append("interface\n")
            append("\n")
            append("implementation\n")
            append("\n")
            append("end.\n")
        }
    }

    /**
     * Turns a (file) name into a valid Pascal identifier: keep letters, digits and underscores,
     * prefix a leading digit, and fall back to [fallback] when nothing usable remains.
     */
    internal fun toIdentifier(name: String, fallback: String): String {
        val sanitized = name.filter { it.isLetterOrDigit() || it == '_' }
        return when {
            sanitized.isEmpty() -> fallback
            sanitized.first().isDigit() -> "P$sanitized"
            else -> sanitized
        }
    }
}
