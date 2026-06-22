package org.pcsoft.passat.plugin.wizard

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

/**
 * Generates a minimal starter Object Pascal program for newly created Pascal modules. The emitted
 * source matches exactly the empty-program grammar the `:language` core already parses
 * (`program <Name>; begin end.`), so it highlights without errors out of the box.
 */
object SampleCodeGenerator {
    private val LOG = logger<SampleCodeGenerator>()

    /**
     * Creates `<sourceRoot>/<programName>.pas` with a minimal program. [programName] is sanitized to
     * a valid Pascal identifier. Must be called inside the EDT; performs its own write action.
     */
    fun generate(sourceRoot: VirtualFile, programName: String) {
        val identifier = toIdentifier(programName)
        val content = buildString {
            append("program ").append(identifier).append(";\n")
            append("begin\n")
            append("end.\n")
        }
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

    internal fun toIdentifier(name: String): String {
        val sanitized = name.filter { it.isLetterOrDigit() || it == '_' }
        return when {
            sanitized.isEmpty() -> "Program1"
            sanitized.first().isDigit() -> "P$sanitized"
            else -> sanitized
        }
    }
}
