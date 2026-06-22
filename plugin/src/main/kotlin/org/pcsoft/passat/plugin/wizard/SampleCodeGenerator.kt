package org.pcsoft.passat.plugin.wizard

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

/**
 * Generates a minimal starter Object Pascal program for newly created Pascal modules. The emitted
 * source uses the standard `SysUtils` unit, so the new module immediately demonstrates `uses`
 * completion and resolution (the unit resolves against the module's FPC SDK roots).
 */
object SampleCodeGenerator {
    private val LOG = logger<SampleCodeGenerator>()

    /**
     * Creates `<sourceRoot>/<programName>.pas` with a minimal program. [programName] is sanitized to
     * a valid Pascal identifier. Must be called inside the EDT; performs its own write action.
     */
    fun generate(sourceRoot: VirtualFile, programName: String) {
        val identifier = toIdentifier(programName)
        val content = buildProgramSource(identifier)
        try {
            runWriteAction {
                val file = sourceRoot.findChild("$identifier.pas")
                    ?: sourceRoot.createChildData(this, "$identifier.pas")
                VfsUtil.saveText(file, content)
            }
        } catch (e: Exception) {
            LOG.warn("Failed to generate sample Pascal program in ${sourceRoot.path}", e)
        }
    }

    /** The starter program source: a header, a `uses SysUtils;` import and an empty body. */
    internal fun buildProgramSource(identifier: String): String = buildString {
        append("program ").append(identifier).append(";\n")
        append("\n")
        append("uses\n")
        append("  SysUtils;\n")
        append("\n")
        append("begin\n")
        append("end.\n")
    }

    internal fun toIdentifier(name: String): String {
        val sanitized = name.filter { it.isLetterOrDigit() || it == '_' }
        return when {
            sanitized.isEmpty() -> "Program1"
            sanitized.first().isDigit() -> "P$sanitized"
            else -> sanitized
        }
    }
}
