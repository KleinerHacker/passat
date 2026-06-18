package org.pcsoft.passat.language

import com.intellij.lang.Language

/**
 * The Object Pascal [Language] instance shared by all parsing and PSI-level features of the
 * reusable language core. Everything in this module hangs off this language id.
 */
object ObjectPascalLanguage : Language("ObjectPascal") {
    private fun readResolve(): Any = ObjectPascalLanguage

    override fun getDisplayName(): String = "Object Pascal"
}
