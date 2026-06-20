package org.pcsoft.passat.language.psi

import com.intellij.psi.tree.IElementType
import org.pcsoft.passat.language.ObjectPascalLanguage

/**
 * Composite (rule) element type produced by the Object Pascal parser. Referenced by the generated
 * grammar (`elementTypeClass` in `ObjectPascal.bnf`).
 */
class ObjectPascalElementType(debugName: String) : IElementType(debugName, ObjectPascalLanguage)
