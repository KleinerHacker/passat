package org.pcsoft.passat.language.psi

import com.intellij.psi.tree.IElementType
import org.pcsoft.passat.language.ObjectPascalLanguage

/**
 * Token (leaf) element type produced by the Object Pascal lexer. Referenced by the generated
 * grammar (`tokenTypeClass` in `ObjectPascal.bnf`).
 */
class ObjectPascalTokenType(debugName: String) : IElementType(debugName, ObjectPascalLanguage) {
    override fun toString(): String = "ObjectPascalTokenType.${super.toString()}"
}
